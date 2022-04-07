package io.logto.sdk.android.auth.logto

import android.net.Uri

internal object LogtoAuthManager {
    private var logtoAuthCallbackUriScheme: String? = null
    private val logtoAuthSession = mutableMapOf<String, LogtoAuthSession>()

    fun handleAuthStart(scheme: String, authSession: LogtoAuthSession) {
        logtoAuthSession[scheme] = authSession
        logtoAuthCallbackUriScheme = scheme
    }

    fun handleCallbackUri(uri: Uri) {
        logtoAuthSession.remove(uri.scheme)?.handleCallbackUri(uri)
    }

    fun handleUserCancel() {
        logtoAuthSession.remove(logtoAuthCallbackUriScheme)?.handleUserCancel()
    }

    fun isLogtoAuthCallbackUriScheme(uriScheme: String?): Boolean {
        return uriScheme?.let {
            logtoAuthSession.containsKey(it)
        } ?: false
    }
}
