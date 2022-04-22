package io.logto.sdk.android.auth.social

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.auth.social.alipay.AlipaySocialSession
import io.logto.sdk.android.auth.social.web.WebSocialSession
import io.logto.sdk.android.auth.social.wechat.WechatSocialSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.util.LogtoUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SocialSessionHelperTest {

    @Test
    fun createSocialSession() {
        val mockContext: Activity = mockk()
        val callbackUri = "https://logto.dev/callback"
        val mockCompletion: Completion<SocialException, String> = mockk()

        val httpRedirectTo = "http://github.com/login"
        val httpSocialSession = SocialSessionHelper.createSocialSession(
            mockContext,
            httpRedirectTo,
            callbackUri,
            mockCompletion
        )
        assertThat(httpSocialSession).isInstanceOf(WebSocialSession::class.java)

        val httpsRedirectTo = "https://github.com/login"
        val httpsSocialSession = SocialSessionHelper.createSocialSession(
            mockContext,
            httpsRedirectTo,
            callbackUri,
            mockCompletion
        )
        assertThat(httpsSocialSession).isInstanceOf(WebSocialSession::class.java)

        val alipayRedirectTo = "alipay://appid/path"
        val alipaySocialSession = SocialSessionHelper.createSocialSession(
            mockContext,
            alipayRedirectTo,
            callbackUri,
            mockCompletion
        )
        assertThat(alipaySocialSession).isInstanceOf(AlipaySocialSession::class.java)

        val redirectToWechat = "wechat://appid/path"
        val wechatSocialSession = SocialSessionHelper.createSocialSession(
            mockContext,
            redirectToWechat,
            callbackUri,
            mockCompletion
        )
        assertThat(wechatSocialSession).isInstanceOf(WechatSocialSession::class.java)
    }

    @Test
    fun getSupportedSocialConnectorIds() {
        mockkObject(LogtoUtils)
        every {
            LogtoUtils.isDependencyInstalled(AlipaySocialSession.SDK_IDENTIFY_CLASS_NAME)
        } returns false

        every {
            LogtoUtils.isDependencyInstalled(WechatSocialSession.SDK_IDENTIFY_CLASS_NAME)
        } returns true

        val supportedSocialConnectorIds = SocialSessionHelper.getSupportedSocialConnectorIds()
        assertThat(supportedSocialConnectorIds).apply {
            doesNotContain(AlipaySocialSession.CONNECTOR_ID)
            contains(WechatSocialSession.CONNECTOR_ID)
        }
    }
}
