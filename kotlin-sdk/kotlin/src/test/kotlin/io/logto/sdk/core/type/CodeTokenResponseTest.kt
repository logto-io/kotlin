package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CodeTokenResponseTest {
    private val accessToken = "accessToken"
    private val refreshToken = "refreshToken"
    private val idToken = "idToken"
    private val scope = "scope"
    private val expiresIn = 60L

    private val codeTokenResponse = CodeTokenResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        scope = scope,
        expiresIn = expiresIn
    )

    @Test
    fun `CodeTokenResponse should get expected access token`() {
        assertThat(codeTokenResponse.accessToken).isEqualTo(accessToken)
    }

    @Test
    fun `CodeTokenResponse should get expected refresh token`() {
        assertThat(codeTokenResponse.refreshToken).isEqualTo(refreshToken)
    }

    @Test
    fun `CodeTokenResponse should get expected id token`() {
        assertThat(codeTokenResponse.idToken).isEqualTo(idToken)
    }

    @Test
    fun `CodeTokenResponse should get expected scope`() {
        assertThat(codeTokenResponse.scope).isEqualTo(scope)
    }

    @Test
    fun `CodeTokenResponse should get expected expiresIn`() {
        assertThat(codeTokenResponse.expiresIn).isEqualTo(expiresIn)
    }
}
