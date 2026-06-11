package io.logto.sdk.android.auth.logto

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.exception.LogtoException
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
        LogtoAuthManager.browserSession = null
    }

    @Test
    fun `handleAuthStart should cache current browser session`() {
        val mockBrowserSession: LogtoBrowserSession = mockk()
        LogtoAuthManager.handleAuthStart(mockBrowserSession)
        assertThat(LogtoAuthManager.browserSession).isEqualTo(mockBrowserSession)
    }

    @Test
    fun `handleCallbackUri should invoke the handleCallbackUri method in the session and clear the session cache`() {
        val mockBrowserSession: LogtoBrowserSession = mockk()
        every { mockBrowserSession.handleCallbackUri(any()) } just Runs
        val mockCallbackUri: Uri = mockk()

        LogtoAuthManager.handleAuthStart(mockBrowserSession)
        LogtoAuthManager.handleCallbackUri(mockCallbackUri)

        verify {
            mockBrowserSession.handleCallbackUri(any())
        }
        assertThat(LogtoAuthManager.browserSession).isNull()
    }

    @Test
    fun `handleUserCancel should invoke the handleUserCancel method in the session and clear session cache`() {
        val mockBrowserSession: LogtoBrowserSession = mockk()
        every { mockBrowserSession.handleUserCancel() } just Runs

        LogtoAuthManager.handleAuthStart(mockBrowserSession)
        LogtoAuthManager.handleUserCancel()

        verify {
            mockBrowserSession.handleUserCancel()
        }
        assertThat(LogtoAuthManager.browserSession).isNull()
    }

    @Test
    fun `handleFailure should invoke the handleFailure method in the session and clear session cache`() {
        val mockBrowserSession: LogtoBrowserSession = mockk()
        every { mockBrowserSession.handleFailure(any()) } just Runs

        LogtoAuthManager.handleAuthStart(mockBrowserSession)
        LogtoAuthManager.handleFailure(LogtoException(LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER))

        verify {
            mockBrowserSession.handleFailure(any())
        }
        assertThat(LogtoAuthManager.browserSession).isNull()
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
    fun `isLogtoAuthResult should normalize URI scheme host and path`() {
        val redirectUri = "io.logto.android://IO.LOGTO.SAMPLE/callback%7E"
        val callbackUri = Uri.parse("IO.LOGTO.ANDROID://io.logto.sample/callback~?state=state&code=code")

        val logtoAuthSession = LogtoAuthSession(
            mockk(),
            mockk(),
            mockk(),
            SignInOptions(redirectUri = redirectUri),
            mockk(),
        )

        LogtoAuthManager.handleAuthStart(logtoAuthSession)
        assertThat(LogtoAuthManager.isLogtoAuthResult(callbackUri)).isTrue()
    }

    @Test
    fun `isLogtoAuthResult should keep user info case-sensitive when normalizing authority`() {
        val redirectUri = "io.logto.android://User@IO.LOGTO.SAMPLE/callback"
        val callbackUri = Uri.parse("io.logto.android://user@io.logto.sample/callback?state=state&code=code")

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
        assertThat(LogtoAuthManager.browserSession).isNull()
        assertThat(LogtoAuthManager.isLogtoAuthResult(mockk())).isFalse()
    }
}
