package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LogtoWebViewAuthClient(
    private val hostActivity: Activity,
    private val injectScript: String,
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.evaluateJavascript(injectScript, null)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (view == null || request == null) {
            return super.shouldOverrideUrlLoading(view, request)
        }

        request.apply {
            if (LogtoAuthManager.isLogtoAuthResult(url)) {
                LogtoAuthManager.handleCallbackUri(url)
                hostActivity.finish()
                return true
            }
        }

        return false
    }
}
