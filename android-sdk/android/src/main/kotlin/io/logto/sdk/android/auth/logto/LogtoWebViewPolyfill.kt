package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface

class LogtoWebViewPolyfill(private val hostActivity: Activity) {
    companion object {
        const val NAME = "Polyfill"
    }

    fun getInjectScript() = """
        window.addEventListener("load", () => {
            window.navigator.clipboard = {
                writeText: (text) => window.$NAME.writeText(text),
            };
        });
    """.trimIndent()

    @JavascriptInterface
    fun writeText(text: String) {
        val clipboard = hostActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(text, text))
    }
}
