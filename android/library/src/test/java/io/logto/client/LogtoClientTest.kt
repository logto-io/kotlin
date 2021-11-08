package io.logto.client

import com.google.common.truth.Truth.assertThat
import io.ktor.http.Url
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.CodeChallengeMethod
import io.logto.client.constant.PromptValue
import io.logto.client.constant.QueryKey
import io.logto.client.constant.ResourceValue
import io.logto.client.constant.ResponseType
import io.logto.client.model.OidcConfiguration
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import java.util.UUID

class LogtoClientTest {

    private lateinit var mockedLogtoConfig: LogtoConfig
    private lateinit var logtoClient: LogtoClient

    @Before
    fun setUp() {
        mockedLogtoConfig = mock()
        `when`(mockedLogtoConfig.clientId).thenReturn("clientId")
        `when`(mockedLogtoConfig.redirectUri).thenReturn("redirectUri")
        `when`(mockedLogtoConfig.encodedScopes).thenReturn("encodedScopes")

        logtoClient = LogtoClient(mockedLogtoConfig)
    }

    @Test
    fun getSignInUrl() {
        val oidcConfiguration: OidcConfiguration = mock()
        `when`(oidcConfiguration.authorizationEndpoint)
            .thenReturn("https://logto.dev/oidc/auth")
        val codeChallenge = UUID.randomUUID().toString()
        val signInUrlString = logtoClient.getSignInUrl(oidcConfiguration, codeChallenge)
        val url = Url(signInUrlString)
        url.apply {
            assertThat(protocol.name).isEqualTo("https")
            assertThat(host).isEqualTo("logto.dev")
            assertThat(encodedPath).isEqualTo("/oidc/auth")
            assertThat(parameters[QueryKey.CLIENT_ID]).isEqualTo(mockedLogtoConfig.clientId)
            assertThat(parameters[QueryKey.CODE_CHALLENGE]).isEqualTo(codeChallenge)
            assertThat(parameters[QueryKey.CODE_CHALLENGE_METHOD]).isEqualTo(CodeChallengeMethod.S256)
            assertThat(parameters[QueryKey.PROMPT]).isEqualTo(PromptValue.CONSENT)
            assertThat(parameters[QueryKey.RESPONSE_TYPE]).isEqualTo(ResponseType.CODE)
            assertThat(parameters[QueryKey.SCOPE]).isEqualTo(mockedLogtoConfig.encodedScopes)
            assertThat(parameters[QueryKey.RESOURCE]).isEqualTo(ResourceValue.LOGTO_API)
        }
    }
}
