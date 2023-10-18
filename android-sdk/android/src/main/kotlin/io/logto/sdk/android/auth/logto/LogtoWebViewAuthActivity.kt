package io.logto.sdk.android.auth.logto

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class LogtoWebViewAuthActivity : AppCompatActivity() {
    internal lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val uri = intent.getStringExtra(EXTRA_URI)

        if (uri == null) {
            finish()
            return
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE

            val polyfill = LogtoWebViewPolyfill(hostActivity = this@LogtoWebViewAuthActivity)
            addJavascriptInterface(polyfill, LogtoWebViewPolyfill.NAME)

            val socialHandler = LogtoWebViewSocialHandler(
                webView = this,
                hostActivity = this@LogtoWebViewAuthActivity,
            )
            addJavascriptInterface(
                socialHandler,
                LogtoWebViewSocialHandler.NAME,
            )

            val injectScript = polyfill.getInjectScript() + socialHandler.getInjectScript()

            webViewClient = LogtoWebViewAuthClient(
                hostActivity = this@LogtoWebViewAuthActivity,
                injectScript = injectScript,
            )
        }
        webView.loadUrl(uri)
        setContentView(webView)
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
                Intent(context, LogtoWebViewAuthActivity::class.java).apply {
                    putExtra(EXTRA_URI, uri)
                },
            )
        }
    }
}
