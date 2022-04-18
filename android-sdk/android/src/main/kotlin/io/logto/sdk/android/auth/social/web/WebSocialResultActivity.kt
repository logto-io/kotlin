package io.logto.sdk.android.auth.social.web

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle

class WebSocialResultActivity : Activity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var webSocialSession: WebSocialSession? = null
        fun registerSession(session: WebSocialSession) {
            webSocialSession = session
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            webSocialSession?.handleResult(it)
            webSocialSession = null
        }
        finish()
    }
}
