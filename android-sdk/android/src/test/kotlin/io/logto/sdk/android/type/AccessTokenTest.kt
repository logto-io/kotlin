package io.logto.sdk.android.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AccessTokenTest {
    private val token = "token"
    private val scope = "[\"scope1\", \"scope2\"]"
    private val expiresAt = 0L

    val accessToken = AccessToken(token, scope, expiresAt)

    @Test
    fun `AccessToken should get excepted token`() {
        assertThat(accessToken.token).isEqualTo(token)
    }

    @Test
    fun `AccessToken should get excepted scope`() {
        assertThat(accessToken.scope).isEqualTo(scope)
    }

    @Test
    fun `AccessToken should get excepted expiresAt`() {
        assertThat(accessToken.expiresAt).isEqualTo(expiresAt)
    }
}
