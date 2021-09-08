package io.logto.android.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.authflow.webview.AuthWebViewClient
import io.logto.android.callback.AuthorizationCodeCallback

class WebViewAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        startFlow()
    }

    private fun startFlow() {
        val authUrl = intent.getStringExtra(EXTRA_AUTH_URL)
        val redirectUri = intent.getStringExtra(EXTRA_REDIRECT_URI)
        if (authUrl == null || redirectUri == null) {
            // LOG-67: Catch exceptions in WebView auth flow
            Log.d(TAG, "missing extra EXTRA_AUTH_URL or EXTRA_REDIRECT_URI")
            return
        }
        val webView = makeAuthWebView(redirectUri)
        webView.loadUrl(authUrl)
        setContentView(webView)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun makeAuthWebView(redirectUri: String): WebView {
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = AuthWebViewClient(
            this,
            redirectUri,
            authorizationCodeCallback
        )
        return webView
    }

    companion object {
        private val TAG = WebViewAuthActivity::class.java.simpleName

        private const val EXTRA_AUTH_URL: String = "EXTRA_AUTH_URL"
        private const val EXTRA_REDIRECT_URI: String = "EXTRA_REDIRECT_URI"

        private lateinit var authorizationCodeCallback: AuthorizationCodeCallback

        fun setAuthorizationCodeCallback(authorizationCodeCallback: AuthorizationCodeCallback) {
            WebViewAuthActivity.authorizationCodeCallback = authorizationCodeCallback
        }

        fun makeIntent(
            context: Context,
            authUrl: String,
            redirectUri: String,
        ): Intent {
            val intent = Intent(context, WebViewAuthActivity::class.java)
            intent.putExtra(EXTRA_AUTH_URL, authUrl)
            intent.putExtra(EXTRA_REDIRECT_URI, redirectUri)
            return intent
        }
    }
}
