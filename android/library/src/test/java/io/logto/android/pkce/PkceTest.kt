package io.logto.android.pkce

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.math.ceil

@RunWith(RobolectricTestRunner::class)
class PkceTest {
    @Test
    fun generateCodeVerifierShouldBeFixedLength() {
        val codeVerifier = Pkce.generateCodeVerifier()
        assertThat(codeVerifier.length).isEqualTo(ceil(64 * 1.34).toInt())
    }

    @Test
    fun generateCodeVerifierShouldBeRandomString() {
        val code1 = Pkce.generateCodeVerifier()
        val code2 = Pkce.generateCodeVerifier()
        assertThat(code1).isNotEqualTo(code2)
    }

    @Test
    fun generateCodeChallengeShouldBeFixedLength() {
        val codeVerifier = Pkce.generateCodeVerifier()
        val codeChallenge = Pkce.generateCodeChallenge(codeVerifier)
        assertThat(codeChallenge.length).isEqualTo(ceil(32 * 1.34).toInt())
    }

    @Test
    fun generateCodeChallengeShouldBeDifferentWithDifferentCodeVerifiers() {
        val code1 = Pkce.generateCodeVerifier()
        val codeChallenge1 = Pkce.generateCodeChallenge(code1)

        val code2 = Pkce.generateCodeVerifier()
        val codeChallenge2 = Pkce.generateCodeChallenge(code2)

        assertThat(codeChallenge1).isNotEqualTo(codeChallenge2)
    }

    @Test
    fun generateCodeChallengeWithSpecificCodeShouldBeTheSame() {
        val code = Pkce.generateCodeVerifier()
        assertThat(Pkce.generateCodeChallenge(code)).isEqualTo(Pkce.generateCodeChallenge(code))
    }
}
