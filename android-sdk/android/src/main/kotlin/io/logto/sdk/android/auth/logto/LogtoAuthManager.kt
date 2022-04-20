package io.logto.sdk.android.auth.logto

import android.annotation.SuppressLint
import android.net.Uri

internal object LogtoAuthManager {
    @SuppressLint("StaticFieldLeak")
    private var logtoAuthSession: LogtoAuthSession? = null

    fun handleAuthStart(authSession: LogtoAuthSession) {
        logtoAuthSession = authSession
    }

    fun handleCallbackUri(uri: Uri) {
        logtoAuthSession?.handleCallbackUri(uri)
        logtoAuthSession = null
    }

    fun handleUserCancel() {
        logtoAuthSession?.handleUserCancel()
        logtoAuthSession = null
    }

    fun isLogtoAuthResult(uri: Uri) = logtoAuthSession?.let {
        uri.toString().startsWith(it.redirectUri)
    } ?: false
}
