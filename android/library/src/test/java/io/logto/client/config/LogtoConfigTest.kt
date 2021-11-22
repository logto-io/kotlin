package io.logto.client.config

import com.google.common.truth.Truth.assertThat
import io.logto.client.constant.ScopeValue
import org.junit.Assert.assertThrows
import org.junit.Test

class LogtoConfigTest {
    @Test
    fun construct() {
        val logtoConfig = createTestLogtoConfig()
        assertThat(logtoConfig).isNotNull()
        assertThat(logtoConfig.domain).isEqualTo(TEST_DOMAIN)
        assertThat(logtoConfig.clientId).isEqualTo(TEST_CLIENT_ID)
        assertThat(logtoConfig.scopeValues).isEqualTo(TEST_SCOPE_VALUES)
        assertThat(logtoConfig.redirectUri).isEqualTo(TEST_REDIRECT_URI)
        assertThat(logtoConfig.postLogoutRedirectUri).isEqualTo(TEST_POST_LOGOUT_REDIRE_URI)
    }

    @Test
    fun constructWithEmptyClientIdShouldThrow() {
        val exceptedException = assertThrows(IllegalArgumentException::class.java) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = "",
                scopeValues = TEST_SCOPE_VALUES,
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
        assertThrows(
            "LogtoConfig: scope list should not be empty",
            IllegalArgumentException::class.java,
        ) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = TEST_CLIENT_ID,
                scopeValues = listOf(),
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
    }

    @Test
    fun constructWithEmptyRedirectUriShouldThrow() {
        assertThrows(
            "LogtoConfig: redirectUri should not be empty",
            IllegalArgumentException::class.java,
        ) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = TEST_CLIENT_ID,
                scopeValues = TEST_SCOPE_VALUES,
                redirectUri = "",
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
    }

    @Test
    fun constructWithEmptyPostLogoutRedirectUriShouldThrow() {
        assertThrows(
            "LogtoConfig: postLogoutRedirectUri should not be empty",
            IllegalArgumentException::class.java,
        ) {
            LogtoConfig(
                domain = TEST_DOMAIN,
                clientId = TEST_CLIENT_ID,
                scopeValues = TEST_SCOPE_VALUES,
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = "",
            )
        }
    }

    @Test
    fun constructorWithEmptyDomainShouldThrow() {
        assertThrows(
            "LogtoConfig: domain should not be empty",
            IllegalArgumentException::class.java,
        ) {
            LogtoConfig(
                domain = "",
                clientId = TEST_CLIENT_ID,
                scopeValues = TEST_SCOPE_VALUES,
                redirectUri = TEST_REDIRECT_URI,
                postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
            )
        }
    }

    @Test
    fun encodedScopesValidation() {
        val logtoConfig = createTestLogtoConfig()
        assertThat(logtoConfig.scope)
            .isEqualTo("${ScopeValue.OPEN_ID} ${ScopeValue.OFFLINE_ACCESS}")
    }

    @Test
    fun cacheKeyValidation() {
        val logtoConfig = createTestLogtoConfig()
        assertThat(logtoConfig.cacheKey)
            .isEqualTo("$TEST_CLIENT_ID%3A%3A${ScopeValue.OPEN_ID}+${ScopeValue.OFFLINE_ACCESS}")
    }

    private val TEST_DOMAIN = "logto.dev"
    private val TEST_CLIENT_ID = "clientId"
    private val TEST_SCOPE_VALUES = listOf(ScopeValue.OPEN_ID, ScopeValue.OFFLINE_ACCESS)
    private val TEST_REDIRECT_URI = "redirectUri"
    private val TEST_POST_LOGOUT_REDIRE_URI = "postLogoutRedirectUri"

    private fun createTestLogtoConfig() = LogtoConfig(
        domain = TEST_DOMAIN,
        clientId = TEST_CLIENT_ID,
        scopeValues = TEST_SCOPE_VALUES,
        redirectUri = TEST_REDIRECT_URI,
        postLogoutRedirectUri = TEST_POST_LOGOUT_REDIRE_URI,
    )
}
