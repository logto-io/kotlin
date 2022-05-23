package io.logto.sdk.android.auth.social.wechat

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WechatSocialResultActivityTest {

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `wechat api should handle intent if invoked by the os`() {
        val intent = Intent()
        val activityController = Robolectric.buildActivity(WechatSocialResultActivity::class.java, intent)

        val mockWechatSocialSession: WechatSocialSession = mockk()
        every { mockWechatSocialSession.redirectTo } returns "wechat-native://?app_id=wx1234567890"
        WechatSocialResultActivity.registerSession(mockWechatSocialSession)

        val mockWechatApi: IWXAPI = mockk()
        mockkStatic(WXAPIFactory::class)
        every { WXAPIFactory.createWXAPI(any(), any()) } returns mockWechatApi
        every { mockWechatApi.registerApp(any()) } returns true
        every { mockWechatApi.handleIntent(any(), any()) } returns true

        activityController.create()

        verify {
            mockWechatApi.handleIntent(any(), any())
        }
    }

    @Test
    fun `wechat api should not handle intent without session`() {
        val intent = Intent()
        val activityController = Robolectric.buildActivity(WechatSocialResultActivity::class.java, intent)

        val mockWechatApi: IWXAPI = mockk()
        mockkStatic(WXAPIFactory::class)
        every { WXAPIFactory.createWXAPI(any(), any()) } returns mockWechatApi
        every { mockWechatApi.registerApp(any()) } returns true
        every { mockWechatApi.handleIntent(any(), any()) } returns true

        activityController.create()

        verify(exactly = 0) {
            mockWechatApi.handleIntent(any(), any())
        }
    }

    @Test
    fun `wechat social session should handle missing app id error if no app id is provided`() {
        val intent = Intent()
        val activityController = Robolectric.buildActivity(WechatSocialResultActivity::class.java, intent)

        val mockWechatSocialSession: WechatSocialSession = mockk()
        every { mockWechatSocialSession.redirectTo } returns "wechat-native://?state=testState"
        every { mockWechatSocialSession.handleMissingInformationError() } just Runs
        WechatSocialResultActivity.registerSession(mockWechatSocialSession)

        activityController.create()

        verify {
            mockWechatSocialSession.handleMissingInformationError()
        }

        assertThat(WechatSocialResultActivity.wechatSocialSession).isNull()
    }

    @Test
    fun `onResp should invoke the handleResult method of wechat social session and clear the session cache`() {
        val intent = Intent()
        val activityController = Robolectric.buildActivity(WechatSocialResultActivity::class.java, intent)
        val activity = activityController.get()

        val mockWechatSocialSession: WechatSocialSession = mockk()
        every { mockWechatSocialSession.handleResult(any()) } just Runs
        WechatSocialResultActivity.registerSession(mockWechatSocialSession)
        val mockResponse: BaseResp = mockk()

        activity.onResp(mockResponse)

        verify {
            mockWechatSocialSession.handleResult(mockResponse)
        }
        assertThat(WechatSocialResultActivity.wechatSocialSession).isNull()
    }
}
