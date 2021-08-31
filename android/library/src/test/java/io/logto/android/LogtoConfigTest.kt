package io.logto.android

import io.logto.android.constant.AuthConstant
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
            oidcEndpoint = "oidcEndpoint/",
            scopes = listOf(
                AuthConstant.ScopeValue.OPEN_ID,
                AuthConstant.ScopeValue.OFFLINE_ACCESS,
            ),
            redirectUri = "redirectUri"
        )
    }

    @Test
    fun getClientId() {
        assertThat(logtoConfig.clientId, `is`("clientId"))
    }

    @Test
    fun getOidcEndpoint() {
        assertThat(logtoConfig.oidcEndpoint, `is`("oidcEndpoint/"))
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
    fun getEncodedScopes() {
        assertThat(logtoConfig.encodedScopes, `is`("openid offline_access"))
    }

    @Test
    fun getAuthEndpoint() {
        assertThat(logtoConfig.authEndpoint, `is`("oidcEndpoint/auth"))
    }

    @Test
    fun getTokenEndpoint() {
        assertThat(logtoConfig.tokenEndpoint, `is`("oidcEndpoint/token"))
    }
}
