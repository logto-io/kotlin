package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.logto.sdk.android.auth.social.SocialSessionHelper
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

    fun getInjectSocialScript() = """
        window.logtoNativeSdk = {
            platform: 'android',
            getPostMessage: () => (data) => window.$NAME.postMessage(JSON.stringify(data)),
            supportedSocialConnectorIds: [${getSupportedSocialConnectorIds()}],
            callbackUriScheme: '${hostActivity.packageName}.logto-callback-web',
        };
    """.trimIndent()

    private fun getSupportedSocialConnectorIds() = SocialSessionHelper
        .getSupportedSocialConnectorIds()
        .joinToString(",") { "\"$it\"" }

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
            val socialSession = SocialSessionHelper.createSocialSession(
                scheme = scheme,
                context = hostActivity,
                redirectTo = redirectTo,
                callbackUri = callbackUri,
            ) { exception, signInUri ->
                exception?.let {
                    // TODO - LOG-2186: Handle Errors in Social Sign in Process
                    println("Social == Exceptions Of Social Auth: $exception")
                } ?: webView.loadUrl(requireNotNull(signInUri))
            }

            // TODO - LOG-2186: Handle Errors in Social Sign in Process
            socialSession?.start() ?: print("Social == Unknown Social Scheme: $scheme")
        }
    }
}
