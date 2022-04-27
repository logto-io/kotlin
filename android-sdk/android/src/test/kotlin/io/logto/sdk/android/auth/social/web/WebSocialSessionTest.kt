package io.logto.sdk.android.auth.social.web

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.completion.Completion
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebSocialSessionTest {

    private val mockActivity: Activity = mockk()
    private val mockCompletion: Completion<SocialException, String> = mockk()

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `start should register to web social result activity and invoke an activity to perform signing in`() {
        every { mockActivity.startActivity(any()) } just Runs

        val dummyRedirectTo = "dummyRedirectTo"
        val dummyCallbackUri = "dummyCallbackUri"

        val webSocialSession = WebSocialSession(
            mockActivity,
            dummyRedirectTo,
            dummyCallbackUri,
            mockCompletion
        )

        mockkObject(WebSocialResultActivity.Companion)

        every { WebSocialResultActivity.Companion.registerSession(any()) } just Runs

        webSocialSession.start()

        verify {
            WebSocialResultActivity.Companion.registerSession(webSocialSession)
            mockActivity.startActivity(any())
        }
    }

    @Test
    fun `handleResult should complete with expected continue signing in URI if auth success`() {
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val dummyRedirectTo = "dummyRedirectTo"
        val validCallbackUri = "https://logto.dev/sign-in/github"


        val webSocialSession = WebSocialSession(
            mockActivity,
            dummyRedirectTo,
            validCallbackUri,
            mockCompletion
        )

        val resultUri = Uri.parse("logto-callback://io.logto.test/web?code=testCode")

        webSocialSession.handleResult(resultUri)

        val expectedContinueSigUri = Uri.parse(validCallbackUri).buildUpon().encodedQuery(resultUri.query).build()

        assertThat(expectedContinueSigUri.toString())
            .isEqualTo("https://logto.dev/sign-in/github?code=testCode")

        verify {
            mockCompletion.onComplete(null, expectedContinueSigUri.toString())
        }
    }

    @Test
    fun `handleResult should complete with exception if cannot construct the continue sign in uri`() {
        val socialExceptionCapture = mutableListOf<SocialException?>()
        val continueSignInUriCapture = mutableListOf<String?>()

        every { mockCompletion.onComplete(any(), any()) } just Runs

        val dummyRedirectTo = "dummyRedirectTo"
        val validCallbackUri = "https://logto.dev/sign-in/github"

        val webSocialSession = WebSocialSession(
            mockActivity,
            dummyRedirectTo,
            validCallbackUri,
            mockCompletion
        )

        val resultUri = Uri.parse("logto-callback://io.logto.test/web?code=testCode")

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } throws UnsupportedOperationException()

        webSocialSession.handleResult(resultUri)

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture)
            )
        }

        assertThat(socialExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(SocialException.Type.UNABLE_TO_CONSTRUCT_CALLBACK_URI.code)

        assertThat(continueSignInUriCapture.last()).isNull()
    }
}
