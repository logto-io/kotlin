package io.logto.client

import com.google.common.truth.Truth.assertThat
import io.ktor.http.Url
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.CodeChallengeMethod
import io.logto.client.constant.PromptValue
import io.logto.client.constant.QueryKey
import io.logto.client.constant.ResourceValue
import io.logto.client.constant.ResponseType
import io.logto.client.constant.ScopeValue
import io.logto.client.model.OidcConfiguration
import io.logto.client.service.LogtoService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.UUID

class LogtoClientTest {

    private val testLogtoConfig = LogtoConfig(
        domain = "logto.dev",
        clientId = "clientId",
        scopes = listOf(ScopeValue.OFFLINE_ACCESS, ScopeValue.OPEN_ID),
        redirectUri = "redirectUri",
        postLogoutRedirectUri = "postLogoutRedirectUri"
    )

    private val testOidcConfiguration = OidcConfiguration(
        authorizationEndpoint = "https://logto.dev/oidc/auth",
        tokenEndpoint = "https://logto.dev/oidc/token",
        endSessionEndpoint = "https://logto.dev/oidc/session/end",
        jwksUri = "https://logto.dev/oidc/jwks",
        issuer = "https://logto.dev/oidc",
        revocationEndpoint = "https://logto.dev/oidc/token/revocation"
    )

    @Test
    fun getSignInUrl() {
        val dummyLogtoService: LogtoService = mock()
        val codeChallenge = UUID.randomUUID().toString()
        val logtoClient = LogtoClient(testLogtoConfig, dummyLogtoService)
        val signInUrlStr = logtoClient.getSignInUrl(testOidcConfiguration, codeChallenge)
        Url(signInUrlStr).apply {
            assertThat(protocol.name).isEqualTo("https")
            assertThat(host).isEqualTo("logto.dev")
            assertThat(encodedPath).isEqualTo("/oidc/auth")
            assertThat(parameters[QueryKey.CLIENT_ID]).isEqualTo(testLogtoConfig.clientId)
            assertThat(parameters[QueryKey.CODE_CHALLENGE]).isEqualTo(codeChallenge)
            assertThat(parameters[QueryKey.CODE_CHALLENGE_METHOD]).isEqualTo(CodeChallengeMethod.S256)
            assertThat(parameters[QueryKey.PROMPT]).isEqualTo(PromptValue.CONSENT)
            assertThat(parameters[QueryKey.RESPONSE_TYPE]).isEqualTo(ResponseType.CODE)
            assertThat(parameters[QueryKey.SCOPE]).isEqualTo(testLogtoConfig.encodedScopes)
            assertThat(parameters[QueryKey.RESOURCE]).isEqualTo(ResourceValue.LOGTO_API)
        }
    }

    @Test
    fun getSignOutUrl() {
        val dummyLogtoService: LogtoService = mock()
        val logtoClient = LogtoClient(testLogtoConfig, dummyLogtoService)
        val idToken = UUID.randomUUID().toString()
        val signOutUrlStr = logtoClient.getSignOutUrl(testOidcConfiguration, idToken)
        Url(signOutUrlStr).apply {
            assertThat(protocol.name).isEqualTo("https")
            assertThat(host).isEqualTo("logto.dev")
            assertThat(encodedPath).isEqualTo("/oidc/session/end")
            assertThat(parameters[QueryKey.ID_TOKEN_HINT]).isEqualTo(idToken)
            assertThat(parameters[QueryKey.POST_LOGOUT_REDIRECT_URI])
                .isEqualTo(testLogtoConfig.postLogoutRedirectUri)
        }
    }

    @Test
    fun fetchOidcConfiguration(): Unit = runBlocking {
        val logtoServiceMock: LogtoService = mock()
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        logtoClient.fetchOidcConfiguration()

        verify(logtoServiceMock).fetchOidcConfiguration(eq(testLogtoConfig.domain))
    }

    @Test
    fun grantTokenByAuthorizationCode(): Unit = runBlocking {
        val authorizationCode = UUID.randomUUID().toString()
        val codeVerifier = UUID.randomUUID().toString()
        val logtoServiceMock: LogtoService = mock()
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        logtoClient.grantTokenByAuthorizationCode(
            testOidcConfiguration,
            authorizationCode,
            codeVerifier
        )

        verify(logtoServiceMock).grantTokenByAuthorizationCode(
            tokenEndpoint = eq(testOidcConfiguration.tokenEndpoint),
            clientId = eq(testLogtoConfig.clientId),
            redirectUri = eq(testLogtoConfig.redirectUri),
            code = eq(authorizationCode),
            codeVerifier = eq(codeVerifier)
        )
    }

    @Test
    fun grantTokenByRefreshToken(): Unit = runBlocking {
        val refreshToken = UUID.randomUUID().toString()
        val logtoServiceMock: LogtoService = mock()
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        logtoClient.grantTokenByRefreshToken(
            testOidcConfiguration,
            refreshToken
        )

        verify(logtoServiceMock).grantTokenByRefreshToken(
            tokenEndpoint = eq(testOidcConfiguration.tokenEndpoint),
            clientId = eq(testLogtoConfig.clientId),
            redirectUri = eq(testLogtoConfig.redirectUri),
            refreshToken = eq(refreshToken)
        )
    }

    @Test
    fun fetchJwks(): Unit = runBlocking {
        val logtoServiceMock: LogtoService = mock()
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        logtoClient.fetchJwks(testOidcConfiguration)

        verify(logtoServiceMock).fetchJwks(eq(testOidcConfiguration.jwksUri))
    }
}
