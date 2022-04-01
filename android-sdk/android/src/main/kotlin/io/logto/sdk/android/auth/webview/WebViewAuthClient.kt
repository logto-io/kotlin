package io.logto.sdk.android.auth.webview

import android.app.Activity
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.logto.sdk.android.auth.LogtoAuthManager

class WebViewAuthClient(
    private val hostActivity: Activity,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (view == null || request == null) {
            return super.shouldOverrideUrlLoading(view, request)
        }

        if (LogtoAuthManager.isCurrentAuthingResult(request.url.toString())) {
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
