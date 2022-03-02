package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RefreshTokenTokenResponseTest {
    private val accessToken = "testAccessToken"
    private val refreshToken = "testRefreshToken"
    private val idToken = "testIdToken"
    private val scope = "testScope"
    private val expiresIn = 60L

    private val refreshTokenTokenResponse = RefreshTokenTokenResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        scope = scope,
        expiresIn = expiresIn,
    )

    @Test
    fun getAccessToken() {
        assertThat(refreshTokenTokenResponse.accessToken).isEqualTo(accessToken)
    }

    @Test
    fun getRefreshToken() {
        assertThat(refreshTokenTokenResponse.refreshToken).isEqualTo(refreshToken)
    }

    @Test
    fun getIdToken() {
        assertThat(refreshTokenTokenResponse.idToken).isEqualTo(idToken)
    }

    @Test
    fun getScope() {
        assertThat(refreshTokenTokenResponse.scope).isEqualTo(scope)
    }

    @Test
    fun getExpiresIn() {
        assertThat(refreshTokenTokenResponse.expiresIn).isEqualTo(expiresIn)
    }
}
