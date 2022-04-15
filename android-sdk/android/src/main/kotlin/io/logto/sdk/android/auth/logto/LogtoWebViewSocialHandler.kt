package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.logto.sdk.android.auth.social.SocialSessionFactory
import org.json.JSONObject

class LogtoWebViewSocialHandler(
    private val webView: WebView,
    private val hostActivity: Activity,
) {
    companion object {
        const val NAME = "SocialHandler"

        private object DataKey {
            const val REDIRECT_TO = "redirectTo"
            const val CALLBACK_URI = "callbackUri"
        }
    }

    // TODO - LOG-2178: Add `isAvailable` for social plugins
    // Use ["wechat-native", "alipay"] temporary
    fun getInjectSocialScript() = """
        window.logtoNativeSdk = {
            platform: 'android',
            getPostMessage: () => (data) => window.$NAME.postMessage(JSON.stringify(data)),
            supportedSocialConnectorIds: ["wechat-native", "alipay"],
            callbackUriScheme: '${hostActivity.packageName}.logto-web-social-callback',
        };
    """.trimIndent()

    @JavascriptInterface
    fun postMessage(jsonData: String) {
        // TODO - LOG-2186: Handle Errors in Social Sign in Process
        val data = JSONObject(jsonData)
        val redirectTo = data.getString(DataKey.REDIRECT_TO)
        val callbackUri = data.getString(DataKey.CALLBACK_URI)

        val scheme = Uri.parse(redirectTo).scheme
        if (scheme.isNullOrBlank()) {
            // TODO - LOG-2186: Handle Errors in Social Sign in Process
            return
        }

        webView.post {
            val socialSession = SocialSessionFactory.createSocialSession(
                scheme = scheme,
                context = hostActivity,
                redirectTo = redirectTo,
                callbackUri = callbackUri,
            ) { exception, result ->
                // TODO - LOG-2186: Handle Errors in Social Sign in Process
                println("Social == Exceptions Of Social Auth: $exception")
                println("Social == Result Of Social Auth: $result")
            }

            // TODO - LOG-2186: Handle Errors in Social Sign in Process
            socialSession?.start() ?: print("Social == Unknown Social Scheme: $scheme")
        }
    }
}
