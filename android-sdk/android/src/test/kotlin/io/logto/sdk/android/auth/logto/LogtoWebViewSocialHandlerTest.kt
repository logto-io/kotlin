package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.webkit.WebView
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.auth.social.SocialSessionHelper
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoWebViewSocialHandlerTest {

    private val mockActivity: Activity = mockk()
    private val mockWebView: WebView = mockk()

    @Before
    fun setUp() {
        every { mockActivity.packageName } returns "io.logto.test"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun getInjectSocialScript() {
        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        mockkObject(SocialSessionHelper)
        every {
            SocialSessionHelper.getSupportedSocialConnectorIds()
        } returns mutableListOf("wechat-native", "alipay-native")

        val injectSocialScript = logtoWebViewSocialHandler.getInjectSocialScript()
        verify {
            SocialSessionHelper.getSupportedSocialConnectorIds()
        }

        assertThat(injectSocialScript)
            .isEqualTo(
                """
                window.logtoNativeSdk = {
                    platform: 'android',
                    getPostMessage: () => (data) => window.SocialHandler.postMessage(JSON.stringify(data)),
                    supportedSocialConnectorIds: ['wechat-native', 'alipay-native'],
                    callbackUriScheme: 'logto-callback://io.logto.test/web',
                };
            """.trimIndent()
            )
    }

    @Test
    fun postSocialException() {
        every { mockWebView.evaluateJavascript(any(), any()) } just Runs

        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        val socialException = SocialException(SocialException.Type.UNKNOWN_SOCIAL_SCHEME).apply {
            socialCode = "1001"
            socialMessage = "social message"
        }

        logtoWebViewSocialHandler.postSocialException(socialException)

        verify {
            mockWebView.evaluateJavascript(
                """
                    window.postMessage({
                        type: 'error',
                        code: 'unknown_social_scheme',
                        socialCode: '1001',
                        socialMessage: 'social message',
                    });
                """.trimIndent(),
                null,
            )
        }
    }

    @Test
    fun postMessage() {
        val validJsonDataString =
            "{\"redirectTo\":\"https://github.com/login\",\"callbackUri\":\"https://logto.dev/sign-in/callback/github\"}"

        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        every { mockWebView.post(any()) } answers {
            lastArg<Runnable>().run()
            true
        }

        val mockSocialSession: SocialSession = mockk()
        every { mockSocialSession.start() } just Runs

        mockkObject(SocialSessionHelper)
        every {
            SocialSessionHelper.createSocialSession(
                any(),
                any(),
                any(),
                any()
            )
        } returns mockSocialSession

        logtoWebViewSocialHandler.postMessage(validJsonDataString)

        verify {
            mockSocialSession.start()
        }
    }

    @Test
    fun `postMessage should postSocialException with invalid json data`() {
        val socialExceptionCapture = slot<SocialException>()
        val invalidJsonDataStr = "invalidJson"

        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        mockkObject(logtoWebViewSocialHandler)
        every { logtoWebViewSocialHandler.postSocialException(any()) } just Runs

        logtoWebViewSocialHandler.postMessage(invalidJsonDataStr)

        verify {
            logtoWebViewSocialHandler.postSocialException(
                capture(socialExceptionCapture)
            )
        }

        assertThat(socialExceptionCapture.captured)
            .hasMessageThat()
            .isEqualTo(SocialException.Type.INVALID_JSON.code)
    }

    @Test
    fun `postMessage should postSocialException with invalid redirectUri in json data`() {
        val socialExceptionCapture = slot<SocialException>()
        val validJsonDataWithInvalidRedirectToString =
            "{\"redirectTo\":\"wx121lalsidls2/login\",\"callbackUri\":\"https://logto.dev/sign-in/callback/github\"}"

        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        mockkObject(logtoWebViewSocialHandler)
        every { logtoWebViewSocialHandler.postSocialException(any()) } just Runs

        logtoWebViewSocialHandler.postMessage(validJsonDataWithInvalidRedirectToString)

        verify {
            logtoWebViewSocialHandler.postSocialException(
                capture(socialExceptionCapture)
            )
        }

        assertThat(socialExceptionCapture.captured)
            .hasMessageThat()
            .isEqualTo(SocialException.Type.INVALID_REDIRECT_TO.code)
    }

    @Test
    fun `postMessage should postSocialException with invalid callbackUri in json data`() {
        val socialExceptionCapture = slot<SocialException>()
        val validJsonDataWithInvalidCallbackUriString =
            "{\"redirectTo\":\"https://github.com/login\",\"callbackUri\":\"\"}"

        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        mockkObject(logtoWebViewSocialHandler)
        every { logtoWebViewSocialHandler.postSocialException(any()) } just Runs

        logtoWebViewSocialHandler.postMessage(validJsonDataWithInvalidCallbackUriString)

        verify {
            logtoWebViewSocialHandler.postSocialException(
                capture(socialExceptionCapture)
            )
        }

        assertThat(socialExceptionCapture.captured)
            .hasMessageThat()
            .isEqualTo(SocialException.Type.INVALID_CALLBACK_URI.code)
    }

    @Test
    fun `postMessage should postSocialException with unknown social scheme in redirectTo`() {
        val socialExceptionCapture = slot<SocialException>()
        val validJsonDataWithInvalidRedirectToString =
            "{\"redirectTo\":\"unknown://github.com/login\",\"callbackUri\":\"https://logto.dev/sign-in/callback/github\"}"

        every { mockWebView.post(any()) } answers {
            lastArg<Runnable>().run()
            true
        }

        val logtoWebViewSocialHandler = LogtoWebViewSocialHandler(
            mockWebView,
            mockActivity,
        )

        mockkObject(logtoWebViewSocialHandler)
        every { logtoWebViewSocialHandler.postSocialException(any()) } just Runs

        logtoWebViewSocialHandler.postMessage(validJsonDataWithInvalidRedirectToString)

        verify {
            logtoWebViewSocialHandler.postSocialException(
                capture(socialExceptionCapture)
            )
        }

        assertThat(socialExceptionCapture.captured)
            .hasMessageThat()
            .isEqualTo(SocialException.Type.UNKNOWN_SOCIAL_SCHEME.code)
    }
}
