package io.logto.android.config

import com.google.common.truth.Truth.assertThat
import io.logto.android.constant.ScopeValue
import org.junit.Assert.assertThrows
import org.junit.Test

class LogtoConfigTest {
    @Test
    fun construct() {
        val logtoConfig = createTestLogtoConfig()
        assertThat(logtoConfig).isNotNull()
        assertThat(logtoConfig.domain).isEqualTo(TEST_DOMAIN)
        assertThat(logtoConfig.clientId).isEqualTo(TEST_CLIENT_ID)
        assertThat(logtoConfig.scopes).isEqualTo(TEST_SCOPE_ARRAY)
        assertThat(logtoConfig.redirectUri).isEqualTo(TEST_REDIRECT_URI)
        assertThat(logtoConfig.postLogoutRedirectUri).isEqualTo(TEST_POST_LOGOUT_REDIRE_URI)
    }

    @Test
    fun constructWithEmptyClientIdShouldThrow() {
        val exceptedException = assertThrows(IllegalArgumentException::class.java) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = "",
                scopes = TEST_SCOPE_ARRAY,
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
        assertThat(exceptedException)
            .hasMessageThat()
            .isEqualTo("LogtoConfig: clientId should not be empty")
    }

    @Test
    fun constructWithEmptyScopesShouldThrow() {
        val exceptedException = assertThrows(IllegalArgumentException::class.java) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = TEST_CLIENT_ID,
                scopes = listOf(),
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
        assertThat(exceptedException)
            .hasMessageThat()
            .isEqualTo("LogtoConfig: scope list should not be empty")
    }

    @Test
    fun constructWithEmptyRedirectUriShouldThrow() {
        val exceptedException = assertThrows(IllegalArgumentException::class.java) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = TEST_CLIENT_ID,
                scopes = TEST_SCOPE_ARRAY,
                redirectUri = "",
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
        assertThat(exceptedException)
            .hasMessageThat()
            .isEqualTo("LogtoConfig: redirectUri should not be empty")
    }

    @Test
    fun constructWithEmptyPostLogoutRedirectUriShouldThrow() {
        val exceptedException = assertThrows(IllegalArgumentException::class.java) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = TEST_CLIENT_ID,
                scopes = TEST_SCOPE_ARRAY,
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = "",
            )
        }
        assertThat(exceptedException)
            .hasMessageThat()
            .isEqualTo("LogtoConfig: postLogoutRedirectUri should not be empty")
    }

    @Test
    fun constructorWithEmptyDomainShouldThrow() {
        val exceptedException = assertThrows(IllegalArgumentException::class.java) {
            LogtoConfig(
                domain = "",
                clientId = TEST_CLIENT_ID,
                scopes = TEST_SCOPE_ARRAY,
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
        assertThat(exceptedException)
            .hasMessageThat()
            .isEqualTo("LogtoConfig: domain should not be empty")
    }

    @Test
    fun encodedScopesValidation() {
        val logtoConfig = createTestLogtoConfig()
        assertThat(logtoConfig.encodedScopes)
            .isEqualTo("${ScopeValue.OPEN_ID} ${ScopeValue.OFFLINE_ACCESS}")
    }

    @Test
    fun cacheKeyValidation() {
        val logtoConfig = createTestLogtoConfig()
        assertThat(logtoConfig.cacheKey)
            .isEqualTo("$TEST_CLIENT_ID:${logtoConfig.encodedScopes}")
    }

    private val TEST_DOMAIN = "logto.dev"
    private val TEST_CLIENT_ID = "clientId"
    private val TEST_SCOPE_ARRAY = listOf(ScopeValue.OPEN_ID, ScopeValue.OFFLINE_ACCESS)
    private val TEST_REDIRECT_URI = "redirectUri"
    private val TEST_POST_LOGOUT_REDIRE_URI = "postLogoutRedirectUri"

    private fun createTestLogtoConfig() = LogtoConfig(
        domain = TEST_DOMAIN,
        clientId = TEST_CLIENT_ID,
        scopes = TEST_SCOPE_ARRAY,
        redirectUri = TEST_REDIRECT_URI,
        postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
    )
}
