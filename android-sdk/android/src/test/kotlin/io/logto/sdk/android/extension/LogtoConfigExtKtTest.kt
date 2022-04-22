package io.logto.sdk.android.extension

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.type.LogtoConfig

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoConfigExtKtTest {
    @Test
    fun `should get correct oidc config endpoint by endpoint without slash`() {
        val testLogtoConfig = LogtoConfig(
            endpoint = "https://logto.dev",
            clientId = "client",
        )
        assertThat(testLogtoConfig.oidcConfigEndpoint)
            .isEqualTo("https://logto.dev/oidc/.well-known/openid-configuration")
    }

    @Test
    fun `should get correct oidc config endpoint by endpoint with slash`() {
        val testLogtoConfig = LogtoConfig(
            endpoint = "https://logto.dev/",
            clientId = "client",
        )
        assertThat(testLogtoConfig.oidcConfigEndpoint)
            .isEqualTo("https://logto.dev/oidc/.well-known/openid-configuration")
    }
}
