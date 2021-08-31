package io.logto.android.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PkceUtilTest {
    @Test
    fun generateCodeVerifierAndCodeChallenge_shouldGenerate() {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        assertThat(codeVerifier, `is`(notNullValue()))
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)
        assertThat(codeChallenge, `is`(notNullValue()))
    }
}
