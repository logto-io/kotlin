package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoWebViewAuthClientTest {

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `onPageStarted should call method to inject the script to the page`() {
        val mockActivity:Activity = mockk()
        every { mockActivity.packageName } returns "io.logto.test"

        val injectScript = "injectScript"
        val logtoWebViewAuthClient = LogtoWebViewAuthClient(
            mockActivity,
            injectScript
        )

        mockkObject(logtoWebViewAuthClient)

        every { logtoWebViewAuthClient.callSuperOnPageStarted(any(), any(), any()) } just Runs

        val mockWebView: WebView = mockk()
        every { mockWebView.evaluateJavascript(any(), any()) } just Runs
        val dummyUrl = "dummyUrl"

        logtoWebViewAuthClient.onPageStarted(mockWebView, dummyUrl, mockk())

        verify {
            mockWebView.evaluateJavascript(injectScript, null)
        }
    }

    @Test
    fun `shouldOverrideUrlLoading should handle the auth result and finish the activity if receive the auth result`() {
        val mockActivity:Activity = mockk()
        every { mockActivity.packageName } returns "io.logto.test"
        every { mockActivity.finish() } just Runs

        val injectScript = "injectScript"
        val logtoWebViewAuthClient = LogtoWebViewAuthClient(
            mockActivity,
            injectScript
        )

        val mockWebView: WebView = mockk()
        val mockWebResourceRequest: WebResourceRequest = mockk()
        val logtoAuthResultUri = Uri.parse("io.logto.test://io.logto.test/callback?code=dummycode")
        every { mockWebResourceRequest.url } returns logtoAuthResultUri

        mockkObject(LogtoAuthManager)
        every { LogtoAuthManager.isLogtoAuthResult(logtoAuthResultUri) } returns true
        every { LogtoAuthManager.handleCallbackUri(any()) } just Runs


        assertThat(
            logtoWebViewAuthClient.shouldOverrideUrlLoading(mockWebView, mockWebResourceRequest)
        ).isTrue()

        verify {
            LogtoAuthManager.handleCallbackUri(logtoAuthResultUri)
            mockActivity.finish()
        }
    }
}
