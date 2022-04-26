package io.logto.sdk.android.type

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ReservedScope
import org.junit.Test

class LogtoConfigTest {
    @Test
    fun `LogtoConfig's scope should always contain 'openid' and 'offline_access'`() {
        val logtoConfigWithoutScope = LogtoConfig(
            endpoint = "endpoint",
            appId = "appId",
        )

        assertThat(logtoConfigWithoutScope.scopes).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
        }

        val logtoConfigWithOtherScope = LogtoConfig(
            endpoint = "endpoint",
            appId = "appId",
            scopes = listOf("other_scope")
        )

        assertThat(logtoConfigWithOtherScope.scopes).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
            contains("other_scope")
        }
    }
}
