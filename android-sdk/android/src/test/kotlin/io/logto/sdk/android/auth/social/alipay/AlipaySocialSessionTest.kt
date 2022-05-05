package io.logto.sdk.android.auth.social.alipay

import android.os.Bundle
import com.alipay.sdk.app.OpenAuthTask
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.auth.logto.LogtoWebViewAuthActivity
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.completion.Completion
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlipaySocialSessionTest {
    private val activityController = Robolectric.buildActivity(LogtoWebViewAuthActivity::class.java)

    private val activity = activityController.get()
    private val mockCompletion: Completion<SocialException, String> = mockk()

    private val socialExceptionCapture = mutableListOf<SocialException?>()
    private val continueSignInUriCapture = mutableListOf<String?>()

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `start should execute openAuthTask and complete with expected result`() {
        val redirectTo = "alipay-native://?app_id=1234567890"
        val callbackUri = "https://logto.dev/alipay-native"
        val authorizationCode = "authorizationCode"
        val alipayAuthResult = Bundle().apply {
            putString("auth_code", authorizationCode)
        }

        every { mockCompletion.onComplete(any(), any()) } just Runs

        mockkConstructor(OpenAuthTask::class)

        every {
            anyConstructed<OpenAuthTask>().execute(any(), any(), any(), any(), any())
        } answers {
            arg<OpenAuthTask.Callback>(3).onResult(OpenAuthTask.OK, "errorMessage", alipayAuthResult)
        }

        val alipaySocialSession = AlipaySocialSession(
            activity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        alipaySocialSession.start()

        verify {
            anyConstructed<OpenAuthTask>().execute(any(), any(), any(), any(), any())
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture)
            )
        }

        assertThat(socialExceptionCapture.last()).isNull()
        assertThat(continueSignInUriCapture.last())
            .isEqualTo("$callbackUri?code=$authorizationCode")
    }

    @Test
    fun `should complete with exception if no app id is provided`() {
        val redirectTo = "alipay-native://?state=random"
        val callbackUri = "https://logto.dev/alipay-native"

        every { mockCompletion.onComplete(any(), any()) } just Runs

        val alipaySocialSession = AlipaySocialSession(
            activity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        alipaySocialSession.start()

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture)
            )
        }

        assertThat(socialExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(SocialException.Type.INSUFFICIENT_INFORMATION.code)
        assertThat(continueSignInUriCapture.last()).isNull()
    }

    @Test
    fun `should complete with exception if auth failed`() {
        val redirectTo = "alipay-native://?app_id=1234567890"
        val callbackUri = "https://logto.dev/alipay-native"

        every { mockCompletion.onComplete(any(), any()) } just Runs

        mockkConstructor(OpenAuthTask::class)

        every {
            anyConstructed<OpenAuthTask>().execute(any(), any(), any(), any(), any())
        } answers {
            arg<OpenAuthTask.Callback>(3).onResult(OpenAuthTask.SYS_ERR, "errorMessage", mockk())
        }

        val alipaySocialSession = AlipaySocialSession(
            activity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        alipaySocialSession.start()

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture)
            )
        }

        val capturedSocialException = socialExceptionCapture.last()
        assertThat(capturedSocialException)
            .hasMessageThat()
            .isEqualTo(SocialException.Type.AUTHENTICATION_FAILED.code)
        assertThat(capturedSocialException?.socialCode).isEqualTo(OpenAuthTask.SYS_ERR.toString())
        assertThat(capturedSocialException?.socialMessage).isEqualTo("errorMessage")
        assertThat(continueSignInUriCapture.last()).isNull()
    }
}
