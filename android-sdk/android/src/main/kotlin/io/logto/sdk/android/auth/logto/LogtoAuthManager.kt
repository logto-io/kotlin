package io.logto.sdk.android.auth.logto

import android.annotation.SuppressLint
import android.net.Uri

internal object LogtoAuthManager {
    @SuppressLint("StaticFieldLeak")
    internal var logtoAuthSession: LogtoAuthSession? = null

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

    fun isLogtoAuthResult(uri: Uri) = logtoAuthSession?.let { authSession ->
        uri.matchesRedirectUri(Uri.parse(authSession.signInOptions.redirectUri))
    } ?: false

    private fun Uri.matchesRedirectUri(redirectUri: Uri): Boolean {
        if (!isHierarchical || !redirectUri.isHierarchical) {
            return false
        }

        return scheme == redirectUri.scheme &&
            encodedAuthority == redirectUri.encodedAuthority &&
            encodedPath == redirectUri.encodedPath &&
            redirectUri.queryParameterNames.all { queryKey ->
                getQueryParameters(queryKey) == redirectUri.getQueryParameters(queryKey)
            }
    }
}
