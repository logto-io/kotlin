package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LogtoWebViewAuthClient(
    private val hostActivity: Activity,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (view == null || request == null) {
            return super.shouldOverrideUrlLoading(view, request)
        }

        if (LogtoAuthManager.isLogtoAuthCallbackUriScheme(request.url.scheme)) {
            hostActivity.apply {
                val toCallbackUriActivity = Intent(Intent.ACTION_VIEW).apply {
                    data = request.url
                }
                startActivity(toCallbackUriActivity)
                finish()
            }
            return true
        }

        return false
    }
}
