package io.logto.android.auth.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.constant.QueryKey

class RedirectUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data
        if (data?.getQueryParameter(QueryKey.CODE) !== null) {
            BrowserLoginFlow.onBrowserResult(data)
        }
        finish()
    }
}
