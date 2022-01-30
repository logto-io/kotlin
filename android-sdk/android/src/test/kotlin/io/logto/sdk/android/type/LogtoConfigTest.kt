package io.logto.sdk.android.type

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ReservedScope
import org.junit.Test

class LogtoConfigTest {
    @Test
    fun `LogtoConfig's scope should always contain 'openid' and 'offline_access'`() {
        val logtoConfigWithoutScope = LogtoConfig(
            endpoint = "endpoint",
            clientId = "client",
        )

        assertThat(logtoConfigWithoutScope.scope).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
        }

        val logtoConfigWithOtherScope = LogtoConfig(
            endpoint = "endpoint",
            clientId = "client",
            scope = listOf("other_scope")
        )

        assertThat(logtoConfigWithOtherScope.scope).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
            contains("other_scope")
        }
    }
}
