package io.logto.sdk.android

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.callback.RetrieveCallback
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.OidcConfigResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Test

class LogtoClientTest {
    private val oidcConfigResponseMock: OidcConfigResponse = mockk()

    @Test
    fun getOidcConfig() {
        val logtoConfigMock: LogtoConfig = mockk()
        every { logtoConfigMock.endpoint } returns "https://logto.dev/oidc"

        mockkObject(Core)
        every { Core.fetchOidConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val logtoClient = LogtoClient(logtoConfigMock)

        logtoClient.getOidcConfig(object : RetrieveCallback<OidcConfigResponse> {
            override fun onResult(throwable: Throwable?, result: OidcConfigResponse?) {
                assertThat(throwable).isNull()
                assertThat(result).isEqualTo(oidcConfigResponseMock)
            }
        })
    }

    @Test
    fun `getOidcConfig success more than one time should only fetch once`() {
        val logtoConfigMock: LogtoConfig = mockk()
        every { logtoConfigMock.endpoint } returns "https://logto.dev/oidc"

        mockkObject(Core)
        every { Core.fetchOidConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val logtoClient = LogtoClient(logtoConfigMock)

        logtoClient.getOidcConfig(object : RetrieveCallback<OidcConfigResponse> {
            override fun onResult(throwable: Throwable?, result: OidcConfigResponse?) {
                assertThat(throwable).isNull()
                assertThat(result).isEqualTo(oidcConfigResponseMock)
            }
        })

        logtoClient.getOidcConfig(object : RetrieveCallback<OidcConfigResponse> {
            override fun onResult(throwable: Throwable?, result: OidcConfigResponse?) {
                assertThat(throwable).isNull()
                assertThat(result).isEqualTo(oidcConfigResponseMock)
            }
        })

        verify(exactly = 1) { Core.fetchOidConfig(any(), any()) }
    }
}
