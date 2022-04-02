package io.logto.sdk.android.auth.webview

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.logto.sdk.android.auth.session.WebAuthSession

class WebViewSocialHandler(
    private val webView: WebView,
    private val hostActivity: Activity,
) {
    companion object {
        private const val SOCIAL_HANDLER_NAME = "SocialHandler"

        fun injectToWebView(
            webView: WebView,
            hostActivity: Activity,
        ) {
            webView.addJavascriptInterface(
                WebViewSocialHandler(webView, hostActivity),
                SOCIAL_HANDLER_NAME,
            )
        }
    }

    @JavascriptInterface
    fun postSocialMessage(socialAuthUri: String) {
        // TODO - Conventions Between UI and Native
        webView.post {
            handleSocial(socialAuthUri)
        }
    }

    private fun handleSocial(authUri: String) {
        val webAuthSession = WebAuthSession(hostActivity, authUri) { exception, callbackUri ->
            println("Exceptions Of Web Auth: $exception")
            println("CallbackUri Of Web Auth: $callbackUri")
        }
        webAuthSession.start()
    }
}
