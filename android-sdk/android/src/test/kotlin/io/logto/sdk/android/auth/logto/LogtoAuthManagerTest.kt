package io.logto.sdk.android.auth.logto

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.type.SignInOptions
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoAuthManagerTest {

    @After
    fun tearDown() {
        LogtoAuthManager.logtoAuthSession = null
    }

    @Test
    fun `handleAuthStart should cache current logto auth session`() {
        val mockLogtoAuthSession: LogtoAuthSession = mockk()
        LogtoAuthManager.handleAuthStart(mockLogtoAuthSession)
        assertThat(LogtoAuthManager.logtoAuthSession).isEqualTo(mockLogtoAuthSession)
    }

    @Test
    fun `handleCallbackUri should invoke the handleCallbackUri method in the session and clear the session cache`() {
        val mockLogtoAuthSession: LogtoAuthSession = mockk()
        every { mockLogtoAuthSession.handleCallbackUri(any()) } just Runs
        val mockCallbackUri: Uri = mockk()

        LogtoAuthManager.handleAuthStart(mockLogtoAuthSession)
        LogtoAuthManager.handleCallbackUri(mockCallbackUri)

        verify {
            mockLogtoAuthSession.handleCallbackUri(any())
        }
        assertThat(LogtoAuthManager.logtoAuthSession).isNull()
    }

    @Test
    fun `handleUserCancel should invoke the handleUserCancel method in the session and clear session cache`() {
        val mockLogtoAuthSession: LogtoAuthSession = mockk()
        every { mockLogtoAuthSession.handleUserCancel() } just Runs

        LogtoAuthManager.handleAuthStart(mockLogtoAuthSession)
        LogtoAuthManager.handleUserCancel()

        verify {
            mockLogtoAuthSession.handleUserCancel()
        }
        assertThat(LogtoAuthManager.logtoAuthSession).isNull()
    }

    @Test
    fun `isLogtoAuthResult should return expected result with valid or invalid callback URI`() {
        val redirectUri = "localhost:3001/callback"
        val matchedCallbackUri = Uri.parse(redirectUri)
        val mismatchedCallbackUri = Uri.parse("logto.dev/callback")

        val logtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUri),
            mockk()
        )

        LogtoAuthManager.handleAuthStart(logtoAuthSession)
        assertThat(LogtoAuthManager.isLogtoAuthResult(matchedCallbackUri)).isTrue()
        assertThat(LogtoAuthManager.isLogtoAuthResult(mismatchedCallbackUri)).isFalse()
    }

    @Test
    fun `isLogtoAuthResult should return false if no session is provided`() {
        assertThat(LogtoAuthManager.logtoAuthSession).isNull()
        assertThat(LogtoAuthManager.isLogtoAuthResult(mockk())).isFalse()
    }
}
