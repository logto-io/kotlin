package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IdTokenClaimsTest {
    private val iss = "iss"
    private val sub = "sub"
    private val aud = "aud"
    private val exp = 60L
    private val iat = 1234L
    private val atHash = "atHash"

    private val idTokenClaims = IdTokenClaims(
        iss = iss,
        sub = sub,
        aud = aud,
        exp = exp,
        iat = iat,
        atHash = atHash
    )

    @Test
    fun `IdTokenClaims should get expected iss`() {
        assertThat(idTokenClaims.iss).isEqualTo(iss)
    }

    @Test
    fun `IdTokenClaims should get expected sub`() {
        assertThat(idTokenClaims.sub).isEqualTo(sub)
    }

    @Test
    fun `IdTokenClaims should get expected aud`() {
        assertThat(idTokenClaims.aud).isEqualTo(aud)
    }

    @Test
    fun `IdTokenClaims should get expected exp`() {
        assertThat(idTokenClaims.exp).isEqualTo(exp)
    }

    @Test
    fun `IdTokenClaims should get expected iat`() {
        assertThat(idTokenClaims.iat).isEqualTo(iat)
    }

    @Test
    fun `IdTokenClaims should get expected atHash`() {
        assertThat(idTokenClaims.atHash).isEqualTo(atHash)
    }
}
