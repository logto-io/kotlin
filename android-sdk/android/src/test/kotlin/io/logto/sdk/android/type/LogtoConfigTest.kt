package io.logto.sdk.android.type

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ReservedResource
import io.logto.sdk.core.constant.ReservedScope
import io.logto.sdk.core.constant.UserScope
import io.logto.sdk.core.util.TokenUtils
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
            contains(UserScope.PROFILE)
        }

        val logtoConfigWithOtherScope = LogtoConfig(
            endpoint = "endpoint",
            appId = "appId",
            scopes = listOf("other_scope"),
        )

        assertThat(logtoConfigWithOtherScope.scopes).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
            contains(UserScope.PROFILE)
            contains("other_scope")
        }
    }

    @Test
    fun `LogtoConfig's resource should contain 'organization' if organization scope is provided`() {
        val logtoConfig = LogtoConfig(
            endpoint = "endpoint",
            appId = "appId",
            scopes = listOf(UserScope.ORGANIZATIONS),
        )

        assertThat(logtoConfig.resources).contains(ReservedResource.ORGANIZATION)
    }

    @Test
    fun `LogtoConfig should use the default ID token verification options`() {
        val logtoConfig = LogtoConfig(
            endpoint = "endpoint",
            appId = "appId",
        )

        assertThat(logtoConfig.idTokenVerification.clockTolerance)
            .isEqualTo(TokenUtils.DEFAULT_CLOCK_TOLERANCE_IN_SECONDS)
    }

    @Test
    fun `LogtoConfig should keep the custom ID token verification options`() {
        val idTokenVerification = IdTokenVerificationOptions(clockTolerance = 600)
        val logtoConfig = LogtoConfig(
            endpoint = "endpoint",
            appId = "appId",
            idTokenVerification = idTokenVerification,
        )

        assertThat(logtoConfig.idTokenVerification).isSameInstanceAs(idTokenVerification)
    }
}
