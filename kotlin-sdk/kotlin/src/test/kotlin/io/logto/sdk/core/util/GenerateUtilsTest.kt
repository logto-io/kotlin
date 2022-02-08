package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenerateUtilsTest {
    @Test
    fun generateCodeVerifierShouldBeRandomString() {
        val code1 = GenerateUtils.generateCodeVerifier()
        val code2 = GenerateUtils.generateCodeVerifier()
        assertThat(code1).isNotEqualTo(code2)
    }

    @Test
    fun generateCodeVerifierShouldLessThen128Characters() {
        val codeVerifier = GenerateUtils.generateCodeVerifier()
        assertThat(codeVerifier.length).isLessThan(128)
    }

    @Test
    fun generateCodeChallengeShouldGenerateCorrectString() {
        assertThat(
            GenerateUtils.generateCodeChallenge(
                "tO6MabnMFRAatnlMa1DdSstypzzkgalL1-k8Hr_GdfTj-VXGiEACqAkSkDhFuAuD8FOU8lMishaXjt29Xt2Oww"
            )
        ).isEqualTo("0K3SLeGlNNzFswYJjcVzcN4C76m_8NZORxFJLBJWGwg")

        assertThat(
            GenerateUtils.generateCodeChallenge("ipK7uh7F41nJyYY4RZQzEwBwBTd-BlXSO4W8q0tK5VA")
        ).isEqualTo("C51JGVPSnuLTTumLt6X5w2JAL_kBaeqHON3KPIviYaU")

        assertThat(
            GenerateUtils.generateCodeChallenge("Á")
        ).isEqualTo("p3yvZiKYauPicLIDZ0W1peDz4Z9KFC-9uxtDfoO1KOQ")

        assertThat(
            GenerateUtils.generateCodeChallenge("🚀")
        ).isEqualTo("67wLKHDrMj8rbP-lxJPO74GufrNq_HPU4DZzAWMdrsU")
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

    @Test
    fun generateStateShouldLessThen128Characters() {
        val state = GenerateUtils.generateState()
        assertThat(state.length).isLessThan(128)
    }
}
