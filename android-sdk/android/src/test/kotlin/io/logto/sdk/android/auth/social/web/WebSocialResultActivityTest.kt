package io.logto.sdk.android.auth.social.web

import android.content.Intent
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebSocialResultActivityTest {

    @After
    fun tearDown() {
        WebSocialResultActivity.webSocialSession = null
    }

    @Test
    fun `web social session should have been registered successfully`() {
        val mockWebSocialSession: WebSocialSession = mockk()
        WebSocialResultActivity.registerSession(mockWebSocialSession)
        assertThat(WebSocialResultActivity.webSocialSession).isEqualTo(mockWebSocialSession)
    }

    @Test
    fun `should process the social session if this activity is invoked by the os with result`() {
        val mockWebSocialSession: WebSocialSession = mockk()
        every { mockWebSocialSession.handleResult(any()) } just Runs
        WebSocialResultActivity.registerSession(mockWebSocialSession)

        val resultUri = Uri.parse("logto-callback://io.logto.test/web?code=testCode")
        val resultIntent = Intent().apply {
            data = resultUri
        }
        val activityController = Robolectric.buildActivity(WebSocialResultActivity::class.java, resultIntent)
        activityController.create()

        verify {
            mockWebSocialSession.handleResult(resultUri)
        }

        assertThat(WebSocialResultActivity.webSocialSession).isNull()
    }

    @Test
    fun `should not process the social session if this activity is invoked by the os without result`() {
        val mockWebSocialSession: WebSocialSession = mockk()
        every { mockWebSocialSession.handleResult(any()) } just Runs
        WebSocialResultActivity.registerSession(mockWebSocialSession)

        val resultIntent = Intent()

        val activityController = Robolectric.buildActivity(WebSocialResultActivity::class.java, resultIntent)
        activityController.create()

        verify(exactly = 0) {
            mockWebSocialSession.handleResult(any())
        }

        assertThat(WebSocialResultActivity.webSocialSession).isNotNull()
    }
}
