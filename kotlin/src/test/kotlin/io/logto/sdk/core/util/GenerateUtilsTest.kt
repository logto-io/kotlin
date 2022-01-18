package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.ceil

class GenerateUtilsTest {
    @Test
    fun generateCodeVerifierShouldBeFixedLength() {
        val codeVerifier = GenerateUtils.generateCodeVerifier()
        assertThat(codeVerifier.length).isEqualTo(ceil(64 * 1.34).toInt())
    }

    @Test
    fun generateCodeVerifierShouldBeRandomString() {
        val code1 = GenerateUtils.generateCodeVerifier()
        val code2 = GenerateUtils.generateCodeVerifier()
        assertThat(code1).isNotEqualTo(code2)
    }

    @Test
    fun generateCodeChallengeShouldBeFixedLength() {
        val codeVerifier = GenerateUtils.generateCodeVerifier()
        val codeChallenge = GenerateUtils.generateCodeChallenge(codeVerifier)
        assertThat(codeChallenge.length).isEqualTo(ceil(32 * 1.34).toInt())
    }

    @Test
    fun generateCodeChallengeShouldBeDifferentWithDifferentCodeVerifiers() {
        val code1 = GenerateUtils.generateCodeVerifier()
        val codeChallenge1 = GenerateUtils.generateCodeChallenge(code1)

        val code2 = GenerateUtils.generateCodeVerifier()
        val codeChallenge2 = GenerateUtils.generateCodeChallenge(code2)

        assertThat(codeChallenge1).isNotEqualTo(codeChallenge2)
    }

    @Test
    fun generateCodeChallengeWithSpecificCodeShouldBeTheSame() {
        val code = GenerateUtils.generateCodeVerifier()
        assertThat(GenerateUtils.generateCodeChallenge(code)).isEqualTo(GenerateUtils.generateCodeChallenge(code))
    }

    @Test
    fun generateStateShouldNotBeEmpty() {
        val state = GenerateUtils.generateState()
        assertThat(state).isNotEmpty()
    }

    @Test
    fun generateStateShouldBeRandomString() {
        val state1 = GenerateUtils.generateState()
        val state2 = GenerateUtils.generateState()
        assertThat(state1).isNotEqualTo(state2)
    }
}
