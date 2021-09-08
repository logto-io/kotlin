package io.logto.android.authflow.webview

import android.app.Activity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.logto.android.callback.AuthorizationCodeCallback
import io.logto.android.constant.AuthConstant
import io.logto.android.utils.UrlUtil

class AuthWebViewClient(
    private val attachedActivity: Activity,
    private val specifiedRedirectUri: String,
    private val authorizationCodeCallback: AuthorizationCodeCallback,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (view == null || request == null) {
            return super.shouldOverrideUrlLoading(view, request)
        }

        val redirectUrl = request.url.toString()
        if (isAuthRedirectUrl(redirectUrl)) {
            handleAuthRedirect(redirectUrl)
            attachedActivity.finish()
            return true
        }
        return false
    }

    // LOG-67: Catch exceptions in WebView auth flow
    // TBD ...

    private fun isAuthRedirectUrl(redirectUrl: String): Boolean {
        return redirectUrl.startsWith(specifiedRedirectUri)
    }

    private fun handleAuthRedirect(redirectUrl: String) {
        val authCode = UrlUtil.getQueryParam(redirectUrl, AuthConstant.QueryKey.CODE)
        if (authCode == null) {
            authorizationCodeCallback.onFailed(Error("Get Authorization Error"))
            return
        }
        authorizationCodeCallback.onSuccess(authCode)
    }

    internal companion object {
        private val TAG = AuthWebViewClient::class.java.simpleName
    }
}
