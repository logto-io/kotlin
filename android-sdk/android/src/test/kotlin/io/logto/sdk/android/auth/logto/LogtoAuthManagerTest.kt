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
        val redirectUri = "io.logto.android://io.logto.sample/callback"
        val matchedCallbackUri = Uri.parse("$redirectUri?state=state&code=code")
        val mismatchedCallbackUri = Uri.parse("io.logto.android://io.logto.sample/another-callback")

        val logtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUri),
            mockk(),
        )

        LogtoAuthManager.handleAuthStart(logtoAuthSession)
        assertThat(LogtoAuthManager.isLogtoAuthResult(matchedCallbackUri)).isTrue()
        assertThat(LogtoAuthManager.isLogtoAuthResult(mismatchedCallbackUri)).isFalse()
    }

    @Test
    fun `isLogtoAuthResult should return false when callback path is only a prefix match`() {
        val redirectUri = "io.logto.android://io.logto.sample/callback"
        val callbackUri = Uri.parse("io.logto.android://io.logto.sample/callback2?state=state&code=code")

        val logtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUri),
            mockk(),
        )

        LogtoAuthManager.handleAuthStart(logtoAuthSession)
        assertThat(LogtoAuthManager.isLogtoAuthResult(callbackUri)).isFalse()
    }

    @Test
    fun `isLogtoAuthResult should match redirect URI query parameters`() {
        val redirectUri = "io.logto.android://io.logto.sample/callback?connector_id=foo"
        val matchedCallbackUri = Uri.parse("$redirectUri&state=state&code=code")
        val mismatchedCallbackUri = Uri.parse(
            "io.logto.android://io.logto.sample/callback?connector_id=bar&state=state&code=code",
        )

        val logtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUri),
            mockk(),
        )

        LogtoAuthManager.handleAuthStart(logtoAuthSession)
        assertThat(LogtoAuthManager.isLogtoAuthResult(matchedCallbackUri)).isTrue()
        assertThat(LogtoAuthManager.isLogtoAuthResult(mismatchedCallbackUri)).isFalse()
    }

    @Test
    fun `isLogtoAuthResult should return false if callback or redirect URI contains fragment`() {
        val redirectUri = "io.logto.android://io.logto.sample/callback"
        val callbackUriWithFragment = Uri.parse("$redirectUri?state=state&code=code#fragment")

        val logtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUri),
            mockk(),
        )

        LogtoAuthManager.handleAuthStart(logtoAuthSession)
        assertThat(LogtoAuthManager.isLogtoAuthResult(callbackUriWithFragment)).isFalse()

        val redirectUriWithFragment = "$redirectUri#fragment"
        val fragmentLogtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUriWithFragment),
            mockk(),
        )

        LogtoAuthManager.handleAuthStart(fragmentLogtoAuthSession)
        val callbackUri = Uri.parse("$redirectUri?state=state&code=code")
        assertThat(LogtoAuthManager.isLogtoAuthResult(callbackUri)).isFalse()
    }

    @Test
    fun `isLogtoAuthResult should return false if no session is provided`() {
        assertThat(LogtoAuthManager.logtoAuthSession).isNull()
        assertThat(LogtoAuthManager.isLogtoAuthResult(mockk())).isFalse()
    }
}
