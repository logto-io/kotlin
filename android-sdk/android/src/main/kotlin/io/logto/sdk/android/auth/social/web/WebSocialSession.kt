package io.logto.sdk.android.auth.social.web

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.logto.sdk.android.completion.Completion

class WebSocialSession(
    private val context: Activity,
    private val authUri: String,
    private val completion: Completion<String>,
) {
    fun start() {
        WebSocialResultActivity.registerSession(this)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUri)))
    }

    fun handleResult(data: Uri) {
        completion.onComplete(null, data.toString())
    }
}
