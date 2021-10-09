package io.logto.android.auth.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.auth.AuthManager

class RedirectUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data
        data?.let {
            AuthManager.onResult(it)
        }
        finish()
    }
}
