package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.logto.sdk.android.auth.social.alipay.AlipaySocialSession
import io.logto.sdk.android.auth.social.web.WebSocialSession
import io.logto.sdk.android.auth.social.wechat.WechatSocialSession

class LogtoWebViewSocialHandler(
    private val webView: WebView,
    private val hostActivity: Activity,
) {
    companion object {
        const val SOCIAL_HANDLER_NAME = "SocialHandler"
    }

    @JavascriptInterface
    fun postSocialMessage(socialType: String) {
        // TODO - Conventions Between UI and Native
        webView.post {
            handleSocial(socialType)
        }
    }

    private fun handleSocial(socialType: String) {
        println("Social == Social Sign In Click")
        println("Social == SocialType: $socialType")
        // TODO - Create Social Sessions Dynamically
        when (socialType) {
            "web" -> {
                // TODO - Get Auth Uri From UI
                val authUri = "__UI_PLACEHOLDER__"
                val webSocialSession = WebSocialSession(
                    hostActivity,
                    authUri,
                ) { exception, callbackUri ->
                    println("Social == Exceptions Of Web Auth: $exception")
                    println("Social == CallbackUri Of Web Auth: $callbackUri")
                }
                webSocialSession.start()
            }
            "alipay" -> {
                val alipaySocialSession = AlipaySocialSession(
                    context = hostActivity,
                ) { exception, authCode ->
                    println("Social == Exceptions Of Alipay Auth: $exception")
                    println("Social == AuthCode Of Alipay Auth: $authCode")
                }
                alipaySocialSession.start()
            }
            "wechat" -> {
                val wechatAuthSession = WechatSocialSession(
                    context = hostActivity,
                ) { exception, authCode ->
                    println("Social == Exceptions Of Wechat Auth: $exception")
                    println("Social == AuthCode Of Wechat Auth: $authCode")
                }
                wechatAuthSession.start()
            }
        }
    }
}
