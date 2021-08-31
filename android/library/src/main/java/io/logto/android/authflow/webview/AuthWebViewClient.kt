package io.logto.android.authflow.webview

import android.app.Activity
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.logto.android.constant.AuthConstant
import io.logto.android.utils.UrlUtil

class AuthWebViewClient(
    private val attachedActivity: Activity,
    private val specifiedRedirectUri: String
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val redirectUrl = request!!.url.toString()
        if (isAuthRedirectUrl(redirectUrl)) {
            handleAuthRedirect(redirectUrl)
            view!!.destroy()
            attachedActivity.finish()
            return true
        }
        return false
    }

    // LOG-67 Catch exceptions in WebView auth flow
    // TBD ...

    private fun isAuthRedirectUrl(redirectUrl: String): Boolean {
        return redirectUrl.startsWith(specifiedRedirectUri)
    }

    private fun handleAuthRedirect(redirectUrl: String) {
        val authCode = UrlUtil.getQueryParam(redirectUrl, AuthConstant.QueryKey.CODE)
        Log.d(TAG, "Authorization Code: $authCode")
        // LOG-69 Retrieve credentials
    }

    internal companion object {
        private val TAG = AuthWebViewClient::class.java.simpleName
    }
}
