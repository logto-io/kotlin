package io.logto.sdk.android.auth

import android.net.Uri
import io.logto.sdk.android.auth.session.AuthSession
import io.logto.sdk.android.auth.session.LogtoAuthSession

internal object LogtoAuthManager {
    private var logtoAuthCallbackUriScheme: String? = null
    private val delegations = mutableMapOf<String, AuthSession<Any>>()

    fun handleAuthStart(scheme: String, logtoAuthSession: AuthSession<Any>) {
        // Todo - Deduplicate
        delegations[scheme] = logtoAuthSession

        if (logtoAuthSession is LogtoAuthSession) {
            logtoAuthCallbackUriScheme = scheme
        }
    }

    fun handleCallbackUri(uri: Uri) {
        delegations.remove(uri.scheme)?.handleCallbackUri(uri)
    }

    fun handleUserCancel() {
        // TODO - handle user cancel in the real situation of the web auth process
        delegations.remove(logtoAuthCallbackUriScheme)?.handleUserCancel()
    }

    fun isLogtoAuthCallbackUriScheme(uriScheme: String?): Boolean {
        return uriScheme?.let {
            it == logtoAuthCallbackUriScheme
        } ?: false
    }
}
