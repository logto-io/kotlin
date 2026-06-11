package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import io.logto.sdk.android.completion.EmptyCompletion
import io.logto.sdk.android.exception.LogtoException

/**
 * Ends the session on the Logto server by opening the end session endpoint in the browser.
 */
class LogtoSignOutSession(
    private val context: Activity,
    private val signOutUri: String,
    postLogoutRedirectUri: String,
    private val completion: EmptyCompletion<LogtoException>,
) : LogtoBrowserSession {
    override val redirectUri = postLogoutRedirectUri

    fun start() {
        LogtoAuthManager.handleAuthStart(this)
        LogtoBrowserAuthActivity.launch(context, signOutUri)
    }

    override fun handleCallbackUri(callbackUri: Uri) {
        completion.onComplete(null)
    }

    override fun handleUserCancel() {
        completion.onComplete(LogtoException(LogtoException.Type.USER_CANCELED))
    }

    override fun handleFailure(exception: LogtoException) {
        completion.onComplete(exception)
    }
}
