package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.completion.EmptyCompletion
import io.logto.sdk.android.exception.LogtoException
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoSignOutSessionTest {

    private val mockActivity: Activity = mockk()

    private val dummySignOutUri = "https://logto.dev/oidc/session/end?client_id=appId"
    private val dummyPostLogoutRedirectUri = "io.logto.android://io.logto.sample/callback"

    @Before
    fun setUp() {
        every { mockActivity.packageName } returns "logto.test"
        every { mockActivity.startActivity(any()) } just Runs
    }

    @After
    fun tearDown() {
        clearAllMocks()
        LogtoAuthManager.browserSession = null
    }

    private fun createSignOutSession(completion: EmptyCompletion<LogtoException>) = LogtoSignOutSession(
        context = mockActivity,
        signOutUri = dummySignOutUri,
        postLogoutRedirectUri = dummyPostLogoutRedirectUri,
        completion = completion,
    )

    @Test
    fun `redirectUri should be the post logout redirect uri`() {
        val signOutSession = createSignOutSession(mockk())
        assertThat(signOutSession.redirectUri).isEqualTo(dummyPostLogoutRedirectUri)
    }

    @Test
    fun `start should register to the logto auth manager and start an activity`() {
        mockkObject(LogtoAuthManager)

        val signOutSession = createSignOutSession(mockk())
        signOutSession.start()

        verify {
            LogtoAuthManager.handleAuthStart(signOutSession)
        }

        verify {
            mockActivity.startActivity(any())
        }
    }

    @Test
    fun `handleCallbackUri should complete without exception`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val mockCompletion: EmptyCompletion<LogtoException> = mockk()
        every { mockCompletion.onComplete(any()) } just Runs

        val signOutSession = createSignOutSession(mockCompletion)
        signOutSession.handleCallbackUri(Uri.parse("$dummyPostLogoutRedirectUri?extra=param"))

        verify {
            mockCompletion.onComplete(captureNullable(logtoExceptionCapture))
        }
        assertThat(logtoExceptionCapture.last()).isNull()
    }

    @Test
    fun `handleUserCancel should complete with user canceled exception`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val mockCompletion: EmptyCompletion<LogtoException> = mockk()
        every { mockCompletion.onComplete(any()) } just Runs

        val signOutSession = createSignOutSession(mockCompletion)
        signOutSession.handleUserCancel()

        verify {
            mockCompletion.onComplete(captureNullable(logtoExceptionCapture))
        }
        assertThat(logtoExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.USER_CANCELED.name)
    }

    @Test
    fun `handleFailure should complete with the given exception`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val mockCompletion: EmptyCompletion<LogtoException> = mockk()
        every { mockCompletion.onComplete(any()) } just Runs

        val signOutSession = createSignOutSession(mockCompletion)
        signOutSession.handleFailure(LogtoException(LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER))

        verify {
            mockCompletion.onComplete(captureNullable(logtoExceptionCapture))
        }
        assertThat(logtoExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER.name)
    }
}
