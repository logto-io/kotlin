package io.logto.sdk.android.auth.social.web

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.completion.Completion

class WebSocialSession(
    override val context: Activity,
    override val redirectTo: String,
    override val callbackUri: String,
    override val completion: Completion<SocialException, String>,
) : SocialSession {
    override fun start() {
        WebSocialResultActivity.registerSession(this)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirectTo)))
    }

    fun handleResult(data: Uri) {
        val continueSignInUri = try {
            Uri.parse(callbackUri).buildUpon().encodedQuery(data.encodedQuery).build()
        } catch (_: UnsupportedOperationException) {
            completion.onComplete(
                SocialException(SocialException.Type.UNABLE_TO_CONSTRUCT_CALLBACK_URI),
                null,
            )
            return
        }
        completion.onComplete(null, continueSignInUri.toString())
    }
}
