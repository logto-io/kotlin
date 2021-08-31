package io.logto.android

import io.logto.android.authflow.webview.WebViewAuthFlow

class Logto {
    companion object {
        fun webViewAuthFlow() = WebViewAuthFlow()
    }
}
