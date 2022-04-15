package io.logto.sdk.android.auth.social.web

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.completion.Completion

class WebSocialSession(
    override val context: Activity,
    override val redirectTo: String,
    override val callbackUri: String,
    override val completion: Completion<String>,
) : SocialSession {
    override fun start() {
        WebSocialResultActivity.registerSession(this)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirectTo)))
    }

    fun handleResult(data: Uri) {
        completion.onComplete(null, data.toString())
    }
}
