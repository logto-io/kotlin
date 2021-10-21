package io.logto.android.auth.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.auth.AuthManager

class RedirectUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            startActivity(AuthorizationActivity.createHandleCompleteIntent(this))
            AuthManager.onResult(it)
        } ?: finish()
    }
}
