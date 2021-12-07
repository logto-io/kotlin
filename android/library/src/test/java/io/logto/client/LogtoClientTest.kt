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
import io.logto.client.model.TokenSet
import io.logto.client.model.TokenSetParameters
import io.logto.client.service.LogtoService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.jose4j.jwk.JsonWebKeySet
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class LogtoClientTest {

    private val testLogtoConfig = LogtoConfig(
        domain = "logto.dev",
        clientId = "clientId",
        scopeValues = listOf(ScopeValue.OFFLINE_ACCESS, ScopeValue.OPEN_ID),
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
        val dummyLogtoService: LogtoService = mockk()
        val codeChallenge = UUID.randomUUID().toString()
        val state = UUID.randomUUID().toString()
        val logtoClient = LogtoClient(testLogtoConfig, dummyLogtoService)
        val signInUrlStr = logtoClient.getSignInUrl(
            testOidcConfiguration.authorizationEndpoint,
            codeChallenge,
            state,
        )
        Url(signInUrlStr).apply {
            assertThat(protocol.name).isEqualTo("https")
            assertThat(host).isEqualTo("logto.dev")
            assertThat(encodedPath).isEqualTo("/oidc/auth")
            assertThat(parameters[QueryKey.CLIENT_ID]).isEqualTo(testLogtoConfig.clientId)
            assertThat(parameters[QueryKey.CODE_CHALLENGE]).isEqualTo(codeChallenge)
            assertThat(parameters[QueryKey.CODE_CHALLENGE_METHOD]).isEqualTo(CodeChallengeMethod.S256)
            assertThat(parameters[QueryKey.STATE]).isEqualTo(state)
            assertThat(parameters[QueryKey.PROMPT]).isEqualTo(PromptValue.CONSENT)
            assertThat(parameters[QueryKey.RESPONSE_TYPE]).isEqualTo(ResponseType.CODE)
            assertThat(parameters[QueryKey.SCOPE]).isEqualTo(testLogtoConfig.scope)
            assertThat(parameters[QueryKey.RESOURCE]).isEqualTo(ResourceValue.LOGTO_API)
        }
    }

    @Test
    fun getSignOutUrl() {
        val dummyLogtoService: LogtoService = mockk()
        val logtoClient = LogtoClient(testLogtoConfig, dummyLogtoService)
        val idToken = UUID.randomUUID().toString()
        val signOutUrlStr = logtoClient.getSignOutUrl(
            testOidcConfiguration.endSessionEndpoint,
            idToken
        )
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
    fun getOidcConfiguration() = runBlockingTest {
        val logtoServiceMock: LogtoService = mockk()
        val oidcConfigurationMock: OidcConfiguration = mockk(relaxed = true)
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(eq(testLogtoConfig.domain))
        } returns oidcConfigurationMock

        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        val oidcConfiguration = logtoClient.getOidcConfiguration()

        assertThat(oidcConfiguration).isEqualTo(oidcConfigurationMock)
    }

    @Test
    fun getOidcConfigurationMoreThenOnceShouldJustFetchOnce() = runBlockingTest {
        val logtoServiceMock: LogtoService = mockk()
        val oidcConfigurationMock: OidcConfiguration = mockk(relaxed = true)
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(eq(testLogtoConfig.domain))
        } returns oidcConfigurationMock
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        logtoClient.getOidcConfiguration()
        logtoClient.getOidcConfiguration()

        coVerify(exactly = 1) {
            logtoServiceMock.fetchOidcConfiguration(eq(testLogtoConfig.domain))
        }
    }

    @Test
    fun getOidcConfigurationMoreThenOnceShouldBeTheSameValue() = runBlockingTest {
        val logtoServiceMock: LogtoService = mockk()
        val oidcConfigurationMock: OidcConfiguration = mockk(relaxed = true)
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(eq(testLogtoConfig.domain))
        } returns oidcConfigurationMock
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        val oidcConfiguration1 = logtoClient.getOidcConfiguration()
        val oidcConfiguration2 = logtoClient.getOidcConfiguration()

        assertThat(oidcConfiguration1).isEqualTo(oidcConfigurationMock)
        assertThat(oidcConfiguration2).isEqualTo(oidcConfigurationMock)
        assertThat(oidcConfiguration1).isEqualTo(oidcConfiguration2)
    }

    @Test
    fun grantTokenByAuthorizationCode() = runBlockingTest {
        val logtoServiceMock: LogtoService = mockk()
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(any())
        } returns testOidcConfiguration

        val jwksMock: JsonWebKeySet = mockk()
        coEvery {
            logtoServiceMock.fetchJwks(any())
        } returns jwksMock

        val tokenSetParametersMock: TokenSetParameters = mockk()
        every {
            tokenSetParametersMock.verifyIdToken(any(), any())
        } returns Unit
        coEvery {
            logtoServiceMock.grantTokenByAuthorizationCode(any(), any(), any(), any(), any())
        } returns tokenSetParametersMock

        val tokenSetMock: TokenSet = mockk()
        every {
            tokenSetParametersMock.convertTokenSet()
        } returns tokenSetMock

        val authorizationCode = UUID.randomUUID().toString()
        val codeVerifier = UUID.randomUUID().toString()

        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        val tokenSet = logtoClient.grantTokenByAuthorizationCode(
            testOidcConfiguration.tokenEndpoint,
            authorizationCode,
            codeVerifier
        )

        assertThat(tokenSet).isEqualTo(tokenSetMock)
        coVerify {
            logtoServiceMock.grantTokenByAuthorizationCode(
                tokenEndpoint = eq(testOidcConfiguration.tokenEndpoint),
                clientId = eq(testLogtoConfig.clientId),
                redirectUri = eq(testLogtoConfig.redirectUri),
                code = eq(authorizationCode),
                codeVerifier = eq(codeVerifier)
            )
        }
    }

    @Test
    fun grantTokenByRefreshToken() = runBlockingTest {
        val logtoServiceMock: LogtoService = mockk()
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(any())
        } returns testOidcConfiguration

        val jwksMock: JsonWebKeySet = mockk()
        coEvery {
            logtoServiceMock.fetchJwks(any())
        } returns jwksMock

        val tokenSetParametersMock: TokenSetParameters = mockk()
        coEvery {
            tokenSetParametersMock.verifyIdToken(any(), any())
        } returns Unit
        coEvery {
            logtoServiceMock.grantTokenByRefreshToken(any(), any(), any(), any())
        } returns tokenSetParametersMock

        val tokenSetMock: TokenSet = mockk()
        every {
            tokenSetParametersMock.convertTokenSet()
        } returns tokenSetMock

        val refreshToken = UUID.randomUUID().toString()

        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        val tokenSet = logtoClient.grantTokenByRefreshToken(
            testOidcConfiguration.tokenEndpoint,
            refreshToken
        )

        assertThat(tokenSet).isEqualTo(tokenSetMock)
        coVerify {
            logtoServiceMock.grantTokenByRefreshToken(
                tokenEndpoint = eq(testOidcConfiguration.tokenEndpoint),
                clientId = eq(testLogtoConfig.clientId),
                redirectUri = eq(testLogtoConfig.redirectUri),
                refreshToken = eq(refreshToken)
            )
        }
    }

    @Test
    fun getJsonWebKeySet() = runBlockingTest {
        val jwksMock: JsonWebKeySet = mockk()
        val oidcConfigurationMock: OidcConfiguration = mockk(relaxed = true)
        val logtoServiceMock: LogtoService = mockk()
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(any())
        } returns oidcConfigurationMock
        coEvery {
            logtoServiceMock.fetchJwks(any())
        } returns jwksMock
        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        val jwks = logtoClient.getJsonWebKeySet()

        assertThat(jwks).isEqualTo(jwksMock)
    }

    @Test
    fun getJsonWebKeySetMoreThenOnceShouldJustFetchOnce() = runBlockingTest {
        val jwksMock: JsonWebKeySet = mockk()
        val oidcConfigurationMock: OidcConfiguration = mockk(relaxed = true)
        val logtoServiceMock: LogtoService = mockk()
        coEvery {
            (logtoServiceMock).fetchOidcConfiguration(any())
        } returns oidcConfigurationMock
        coEvery {
            logtoServiceMock.fetchJwks(any())
        } returns jwksMock

        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        logtoClient.getJsonWebKeySet()
        logtoClient.getJsonWebKeySet()

        coVerify(exactly = 1) { logtoServiceMock.fetchJwks(any()) }
    }

    @Test
    fun getJsonWebKeySetMoreThenOnceShouldBeTheSameValue() = runBlockingTest {
        val jwksMock: JsonWebKeySet = mockk()
        val oidcConfigurationMock: OidcConfiguration = mockk(relaxed = true)
        val logtoServiceMock: LogtoService = mockk()
        coEvery {
            logtoServiceMock.fetchOidcConfiguration(any())
        } returns oidcConfigurationMock
        coEvery {
            logtoServiceMock.fetchJwks(any())
        } returns jwksMock

        val logtoClient = LogtoClient(testLogtoConfig, logtoServiceMock)

        val jwks1 = logtoClient.getJsonWebKeySet()
        val jwks2 = logtoClient.getJsonWebKeySet()

        assertThat(jwks1).isEqualTo(jwksMock)
        assertThat(jwks2).isEqualTo(jwksMock)
        assertThat(jwks1).isEqualTo(jwks2)
    }
}
