package io.logto.sdk.android.auth.session

import android.annotation.SuppressLint
import android.net.Uri

object SignInSessionManager {
    @SuppressLint("StaticFieldLeak")
    private var signInSession: SignInSession? = null

    val hasSession: Boolean
        get() = signInSession != null

    fun setSession(session: SignInSession) {
        signInSession = session
    }

    fun handleCallbackUri(callbackUri: Uri) {
        signInSession?.handleCallbackUri(callbackUri.toString())
    }

    fun handleUserCancel() {
        signInSession?.handleUserCancel()
    }

    fun clearSession() {
        signInSession = null
    }
}
