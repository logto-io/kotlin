package io.logto.sdk.android.extension

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.type.LogtoConfig

import org.junit.Test

class LogtoConfigExtKtTest {
    @Test
    fun getOidcConfigEndpoint() {
        val testLogtoConfig = LogtoConfig(
            endpoint = "https://logto.dev",
            clientId = "client",
        )
        assertThat(testLogtoConfig.oidcConfigEndpoint)
            .isEqualTo("https://logto.dev/oidc/.well-known/openid-configuration")
    }
}
