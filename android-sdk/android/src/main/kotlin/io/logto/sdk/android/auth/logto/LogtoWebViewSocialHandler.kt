package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.logto.sdk.android.auth.social.alipay.AlipaySocialSession
import io.logto.sdk.android.auth.social.web.WebSocialSession
import io.logto.sdk.android.auth.social.wechat.WechatSocialSession
import org.json.JSONObject

class LogtoWebViewSocialHandler(
    private val webView: WebView,
    private val hostActivity: Activity,
) {
    companion object {
        const val NAME = "SocialHandler"

        private object DataKey {
            const val REDIRECT_TO = "redirectTo"
        }
    }

    // TODO - LOG-2178: Add `isAvailable` for social plugins
    // Use ["wechat-native", "alipay"] temporary
    fun getInjectSocialScript() = """
        const logtoNativeSdk = {
            platform: 'android',
            getPostMessage: () => (data) => {
                window.$NAME.postMessage(JSON.stringify(data));
            },
            supportedSocialConnectorIds: ["wechat-native", "alipay"],
            callbackUriScheme: ${hostActivity.packageName}.logto-web-social-callback,
        };
    """.trimIndent()

    @JavascriptInterface
    fun postMessage(jsonData: String) {
        // TODO - LOG-2186: Handle Errors in Social Sign in Process
        val data = JSONObject(jsonData)
        val redirectTo = data.getString(DataKey.REDIRECT_TO)
        val redirectToUri = Uri.parse(redirectTo)
        webView.post {
            handleSocial(redirectToUri)
        }
    }

    private fun handleSocial(redirectTo: Uri) {
        when (redirectTo.scheme) {
            "http", "https" -> {
                val authUri = redirectTo.toString()
                val webSocialSession = WebSocialSession(
                    hostActivity,
                    authUri,
                ) { exception, callbackUri ->
                    // TODO - LOG-2186: Handle Errors in Social Sign in Process
                    println("Social == Exceptions Of Web Auth: $exception")
                    println("Social == CallbackUri Of Web Auth: $callbackUri")
                }
                webSocialSession.start()
            }
            "alipay" -> {
                val alipaySocialSession = AlipaySocialSession(
                    context = hostActivity,
                ) { exception, authCode ->
                    // TODO - LOG-2186: Handle Errors in Social Sign in Process
                    println("Social == Exceptions Of Alipay Auth: $exception")
                    println("Social == AuthCode Of Alipay Auth: $authCode")
                }
                alipaySocialSession.start()
            }
            "wechat" -> {
                val wechatAuthSession = WechatSocialSession(
                    context = hostActivity,
                ) { exception, authCode ->
                    // TODO - LOG-2186: Handle Errors in Social Sign in Process
                    println("Social == Exceptions Of Wechat Auth: $exception")
                    println("Social == AuthCode Of Wechat Auth: $authCode")
                }
                wechatAuthSession.start()
            }
            else -> {
                // TODO - LOG-2186: Handle Errors in Social Sign in Process
                println("Social == Unknown Scheme: ${redirectTo.scheme}")
            }
        }
    }
}
