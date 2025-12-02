package io.logto.sdk.android.auth.logto

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

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
        val root = FrameLayout(this)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout(),
            )

            // Only offset top/side to avoid overlaying status bar/cutout; leave bottom as-is so content height stays.
            view.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = 0, // keep bottom content height; no extra inset for nav bar
            )

            WindowInsetsCompat.CONSUMED
        }

        root.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        // Make system bars transparent and choose icon appearance based on light/dark mode.
        val isNightMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, root).apply {
            val useLightIcons = !isNightMode
            isAppearanceLightStatusBars = useLightIcons
            isAppearanceLightNavigationBars = useLightIcons
        }

        webView.loadUrl(uri)
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
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
