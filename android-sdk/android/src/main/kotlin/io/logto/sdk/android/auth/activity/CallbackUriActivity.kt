package io.logto.sdk.android.auth.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.logto.sdk.android.auth.session.SignInSessionManager

class CallbackUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SignInSessionManager.hasSession) {
            intent.data?.let {
                SignInSessionManager.handleCallbackUri(it)
                startActivity(SignInActivity.createRedirectCompleteIntent(this))
            }
        }
        finish()
    }
}
