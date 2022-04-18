package io.logto.sdk.android.auth.logto

import android.annotation.SuppressLint
import android.net.Uri

internal object LogtoAuthManager {
    private var logtoAuthCallbackUriScheme: String? = null
    @SuppressLint("StaticFieldLeak")
    private var logtoAuthSession: LogtoAuthSession? = null

    fun handleAuthStart(scheme: String, authSession: LogtoAuthSession) {
        logtoAuthSession = authSession
        logtoAuthCallbackUriScheme = scheme
    }

    fun handleCallbackUri(uri: Uri) {
        logtoAuthSession?.handleCallbackUri(uri)
        clearSession()
    }

    fun handleUserCancel() {
        logtoAuthSession?.handleUserCancel()
        clearSession()
    }

    fun isLogtoAuthCallbackUriScheme(uriScheme: String?): Boolean {
        return uriScheme?.let {
            uriScheme == logtoAuthCallbackUriScheme
        } ?: false
    }

    private fun clearSession() {
        logtoAuthSession = null
        logtoAuthCallbackUriScheme = null
    }
}
