package io.logto.android.authflow.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RedirectUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data
        BrowserAuthFlow.onBrowserResult(data)
        finish()
    }
}
