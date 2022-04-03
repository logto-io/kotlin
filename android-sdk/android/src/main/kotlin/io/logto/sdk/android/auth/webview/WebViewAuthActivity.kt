package io.logto.sdk.android.auth.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import io.logto.sdk.android.auth.LogtoAuthManager

class WebViewAuthActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val uri = intent.getStringExtra(EXTRA_URI)

        if (uri !== null) {
            webView = WebView(this).apply {
                settings.javaScriptEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webViewClient = WebViewAuthClient(this@WebViewAuthActivity)

                addJavascriptInterface(
                    WebViewSocialHandler(webView, this@WebViewAuthActivity),
                    WebViewSocialHandler.SOCIAL_HANDLER_NAME,
                )
            }

            webView.loadUrl(uri)
            setContentView(webView)

            return
        }

        finish()
    }

    override fun onDestroy() {
        webView.stopLoading()
        LogtoAuthManager.handleUserCancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_URI = "EXTRA_URI"

        fun launch(context: Activity, uri: String) {
            context.startActivity(
                Intent(context, WebViewAuthActivity::class.java).apply {
                    putExtra(EXTRA_URI, uri)
                }
            )
        }
    }
}
