package io.logto.android.auth.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RedirectUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data

        if (BrowserLoginFlow.isInLoginFlow(data)) {
            BrowserLoginFlow.onBrowserResult(data)
            finish()
            return
        }

        if (BrowserLogoutFlow.isInLogoutFlow(data)) {
            BrowserLogoutFlow.onBrowserResult()
            finish()
            return
        }

        finish()
    }
}
