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
    fun getIss() {
        assertThat(idTokenClaims.iss).isEqualTo(iss)
    }

    @Test
    fun getSub() {
        assertThat(idTokenClaims.sub).isEqualTo(sub)
    }

    @Test
    fun getAud() {
        assertThat(idTokenClaims.aud).isEqualTo(aud)
    }

    @Test
    fun getExp() {
        assertThat(idTokenClaims.exp).isEqualTo(exp)
    }

    @Test
    fun getIat() {
        assertThat(idTokenClaims.iat).isEqualTo(iat)
    }

    @Test
    fun getAtHash() {
        assertThat(idTokenClaims.atHash).isEqualTo(atHash)
    }
}
