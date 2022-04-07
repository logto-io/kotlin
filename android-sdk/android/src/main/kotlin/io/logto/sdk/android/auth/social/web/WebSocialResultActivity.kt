package io.logto.sdk.android.auth.social.web

import android.app.Activity
import android.os.Bundle

class WebSocialResultActivity : Activity() {
    companion object {
        private const val WEB_SOCIAL_CALLBACK_SCHEME = "logto-callback"
        private val webSocialSession = mutableMapOf<String, WebSocialSession>()

        fun registerSession(session: WebSocialSession) {
            webSocialSession[WEB_SOCIAL_CALLBACK_SCHEME] = session
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            webSocialSession.remove(WEB_SOCIAL_CALLBACK_SCHEME)?.handleResult(it)
        }
        finish()
    }
}
