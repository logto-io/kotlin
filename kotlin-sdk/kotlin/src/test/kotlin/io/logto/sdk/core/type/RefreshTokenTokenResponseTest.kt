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
        expiresIn = expiresIn
    )

    @Test
    fun `RefreshTokenTokenResponse should get expected access token`() {
        assertThat(refreshTokenTokenResponse.accessToken).isEqualTo(accessToken)
    }

    @Test
    fun `RefreshTokenTokenResponse should get expected refresh token`() {
        assertThat(refreshTokenTokenResponse.refreshToken).isEqualTo(refreshToken)
    }

    @Test
    fun `RefreshTokenTokenResponse should get expected id token`() {
        assertThat(refreshTokenTokenResponse.idToken).isEqualTo(idToken)
    }

    @Test
    fun `RefreshTokenTokenResponse should get expected scope`() {
        assertThat(refreshTokenTokenResponse.scope).isEqualTo(scope)
    }

    @Test
    fun `RefreshTokenTokenResponse should get expected expiresIn`() {
        assertThat(refreshTokenTokenResponse.expiresIn).isEqualTo(expiresIn)
    }
}
