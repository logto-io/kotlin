package io.logto.client.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.ceil

class PkceUtilsTest {
    @Test
    fun generateCodeVerifierShouldBeFixedLength() {
        val codeVerifier = PkceUtils.generateCodeVerifier()
        println("CodeVerifier: $codeVerifier")
        assertThat(codeVerifier.length).isEqualTo(ceil(64 * 1.34).toInt())
    }

    @Test
    fun generateCodeVerifierShouldBeRandomString() {
        val code1 = PkceUtils.generateCodeVerifier()
        val code2 = PkceUtils.generateCodeVerifier()
        assertThat(code1).isNotEqualTo(code2)
    }

    @Test
    fun generateCodeChallengeShouldBeFixedLength() {
        val codeVerifier = PkceUtils.generateCodeVerifier()
        val codeChallenge = PkceUtils.generateCodeChallenge(codeVerifier)
        assertThat(codeChallenge.length).isEqualTo(ceil(32 * 1.34).toInt())
    }

    @Test
    fun generateCodeChallengeShouldBeDifferentWithDifferentCodeVerifiers() {
        val code1 = PkceUtils.generateCodeVerifier()
        val codeChallenge1 = PkceUtils.generateCodeChallenge(code1)

        val code2 = PkceUtils.generateCodeVerifier()
        val codeChallenge2 = PkceUtils.generateCodeChallenge(code2)

        assertThat(codeChallenge1).isNotEqualTo(codeChallenge2)
    }

    @Test
    fun generateCodeChallengeWithSpecificCodeShouldBeTheSame() {
        val code = PkceUtils.generateCodeVerifier()
        assertThat(PkceUtils.generateCodeChallenge(code)).isEqualTo(PkceUtils.generateCodeChallenge(code))
    }
}
