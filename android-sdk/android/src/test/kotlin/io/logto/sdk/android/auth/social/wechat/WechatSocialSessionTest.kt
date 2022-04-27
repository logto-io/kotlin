package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.completion.Completion
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WechatSocialSessionTest {
    private val mockActivity: Activity = mockk()
    private val mockCompletion: Completion<SocialException, String> = mockk()

    private val socialExceptionCapture = mutableListOf<SocialException?>()
    private val continueSignInUriCapture = mutableListOf<String?>()

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun start() {
        val appId = "wx1234567890"
        val redirectTo = "wechat-native://?app_id=$appId"
        val callbackUri = "https://logto.dev/wechat-native"
        val wechatSocialSession = WechatSocialSession(
            mockActivity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        val mockWechatApi: IWXAPI = mockk()

        mockkStatic(WXAPIFactory::class)
        every { WXAPIFactory.createWXAPI(any(), any()) } returns mockWechatApi

        every { mockWechatApi.registerApp(any()) } returns true
        every { mockWechatApi.sendReq(any()) } returns true

        mockkObject(WechatSocialResultActivity.Companion)
        every { WechatSocialResultActivity.registerSession(any()) } just Runs

        wechatSocialSession.start()

        val sendAuthRequestCapture = slot<SendAuth.Req>()
        verify {
            WechatSocialResultActivity.registerSession(wechatSocialSession)
            mockWechatApi.registerApp(appId)
            mockWechatApi.sendReq(capture(sendAuthRequestCapture))
        }

        assertThat(sendAuthRequestCapture.captured).isInstanceOf(SendAuth.Req::class.java)
    }

    @Test
    fun `should complete with exception if no app id is provided`() {
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val appId = ""
        val redirectTo = "wechat-native://?app_id=$appId"
        val callbackUri = "https://logto.dev/wechat-native"
        val wechatSocialSession = WechatSocialSession(
            mockActivity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        wechatSocialSession.start()

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture),
            )
        }

        assertThat(socialExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(SocialException.Type.INSUFFICIENT_INFORMATION.code)
        assertThat(continueSignInUriCapture.last()).isNull()
    }

    @Test
    fun handleResult() {
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val appId = "wx1234567890"
        val redirectTo = "wechat-native://?app_id=$appId"
        val callbackUri = "https://logto.dev/wechat-native"
        val authorizationCode = "authorizationCode"
        val wechatSocialSession = WechatSocialSession(
            mockActivity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        val responseResult = SendAuth.Resp().apply {
            errCode = BaseResp.ErrCode.ERR_OK
            code = authorizationCode
        }

        wechatSocialSession.handleResult(responseResult)

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture),
            )
        }

        val expectedContinueSignInUri = "$callbackUri?code=$authorizationCode"
        assertThat(socialExceptionCapture.last()).isNull()
        assertThat(continueSignInUriCapture.last())
            .isEqualTo(expectedContinueSignInUri)
    }

    @Test
    fun `handleResult should complete with exception if auth failed`() {
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val appId = "wx1234567890"
        val redirectTo = "wechat-native://?app_id=$appId"
        val callbackUri = "https://logto.dev/wechat-native"

        val wechatSocialSession = WechatSocialSession(
            mockActivity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        val responseResult = SendAuth.Resp().apply {
            errCode = BaseResp.ErrCode.ERR_AUTH_DENIED
            errStr = "errorMessage"
        }

        wechatSocialSession.handleResult(responseResult)

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture),
            )
        }

        val capturedSocialException = socialExceptionCapture.last()
        assertThat(capturedSocialException)
            .hasMessageThat()
            .isEqualTo(SocialException.Type.AUTHENTICATION_FAILED.code)
        assertThat(capturedSocialException?.socialCode)
            .isEqualTo(BaseResp.ErrCode.ERR_AUTH_DENIED.toString())
        assertThat(capturedSocialException?.socialMessage).isEqualTo("errorMessage")

        assertThat(continueSignInUriCapture.last()).isNull()
    }

    @Test
    fun handleMissingAppIdError() {
        every { mockCompletion.onComplete(any(), any()) } just Runs
        val appId = ""
        val redirectTo = "wechat-native://?app_id=$appId"
        val callbackUri = "https://logto.dev/wechat-native"
        val wechatSocialSession = WechatSocialSession(
            mockActivity,
            redirectTo,
            callbackUri,
            mockCompletion
        )

        wechatSocialSession.handleMissingAppIdError()

        verify {
            mockCompletion.onComplete(
                captureNullable(socialExceptionCapture),
                captureNullable(continueSignInUriCapture),
            )
        }

        assertThat(socialExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(SocialException.Type.INSUFFICIENT_INFORMATION.code)
        assertThat(continueSignInUriCapture.last()).isNull()
    }
}
