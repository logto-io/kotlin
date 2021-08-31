package io.logto.android.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PkceUtilTest {

    private val verifierCodeSample =
        "LDBUGUHnYWvYINhk9cjZ9HeOHvGlVmnbyQaPW83GzR74-bUPVbUIgw9Z7LZK_YlCDpysR-6EQE3-NFbJLOG2WA"

    @Test
    fun generateCodeVerifier_shouldGenerate() {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        println(codeVerifier)
        assertThat(codeVerifier, `is`(notNullValue()))
    }

    @Test
    fun generateCodeChallenge_shouldGenerate() {
        val codeChallenge = PkceUtil.generateCodeChallenge(verifierCodeSample)
        assertThat(codeChallenge, `is`(notNullValue()))
    }
}
