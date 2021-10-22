package io.logto.android

import io.logto.android.config.LogtoConfig
import io.logto.android.constant.ScopeValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

class LogtoConfigTest {

    private lateinit var logtoConfig: LogtoConfig

    @Before
    fun setUp() {
        logtoConfig = LogtoConfig(
            clientId = "clientId",
            scopes = listOf(
                ScopeValue.OPEN_ID,
                ScopeValue.OFFLINE_ACCESS,
            ),
            redirectUri = "redirectUri",
            postLogoutRedirectUri = "postLogoutRedirectUri",
        )
    }

    @Test
    fun getClientId() {
        assertThat(logtoConfig.clientId, `is`("clientId"))
    }

    @Test
    fun getScopes() {
        assertThat(logtoConfig.scopes, `is`(listOf("openid", "offline_access")))
    }

    @Test
    fun getRedirectUri() {
        assertThat(logtoConfig.redirectUri, `is`("redirectUri"))
    }

    @Test
    fun getPostLogoutRedirectUri() {
        assertThat(logtoConfig.postLogoutRedirectUri, `is`("postLogoutRedirectUri"))
    }

    @Test
    fun getEncodedScopes() {
        assertThat(logtoConfig.encodedScopes, `is`("openid offline_access"))
    }
}
