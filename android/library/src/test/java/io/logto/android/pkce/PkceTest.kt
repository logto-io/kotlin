package io.logto.android.pkce

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PkceTest {
    @Test
    fun generateCodeVerifier() {
        val codeVerifier = Pkce.generateCodeVerifier()
        assertThat(codeVerifier).isNotEmpty()
    }

    @Test
    fun generateCodeVerifierShouldBeRandomString() {
        val code1 = Pkce.generateCodeVerifier()
        val code2 = Pkce.generateCodeVerifier()
        assertThat(code1).isNotEqualTo(code2)
    }

    @Test
    fun generateCodeChallenge() {
        val codeVerifier = Pkce.generateCodeVerifier()
        val codeChallenge = Pkce.generateCodeChallenge(codeVerifier)
        assertThat(codeChallenge).isNotEmpty()
    }

    @Test
    fun generateCodeChallengeShouldBeDifferentWithDifferentCodeVerifiers() {
        val code1 = Pkce.generateCodeVerifier()
        val codeChallenge1 = Pkce.generateCodeChallenge(code1)

        val code2 = Pkce.generateCodeVerifier()
        val codeChallenge2 = Pkce.generateCodeChallenge(code2)

        assertThat(codeChallenge1).isNotEqualTo(codeChallenge2)
    }
}
