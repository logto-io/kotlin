package io.logto.sdk.android.auth.logto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LogtoAuthResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.data?.let {
            LogtoAuthManager.handleCallbackUri(it)
        }

        finish()
    }
}
