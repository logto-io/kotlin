package io.logto.sdk.android.auth.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
        uri?.let {
            webView = WebView(this).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewAuthClient(this@WebViewAuthActivity)
            }
            webView.loadUrl(it)
            setContentView(webView)
        } ?: finish()
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
