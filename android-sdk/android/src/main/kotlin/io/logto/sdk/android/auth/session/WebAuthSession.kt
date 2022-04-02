package io.logto.sdk.android.auth.session

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.logto.sdk.android.auth.LogtoAuthManager
import io.logto.sdk.android.completion.Completion

class WebAuthSession(
    private val context: Activity,
    private val webAuthUri: String,
    override val completion: Completion<String>,
) : AuthSession<String> {
    companion object {
        private const val CALLBACK_URI_SCHEME = "logto-callback"
    }
    override fun start() {
        LogtoAuthManager.handleAuthStart(CALLBACK_URI_SCHEME, this)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webAuthUri)))
    }

    override fun handleCallbackUri(callbackUri: Uri) {
        completion.onComplete(null, callbackUri.toString())
    }

    override fun handleUserCancel() {
        // TODO - handle user cancel in the real situation
        TODO("Not yet implemented")
    }
}
