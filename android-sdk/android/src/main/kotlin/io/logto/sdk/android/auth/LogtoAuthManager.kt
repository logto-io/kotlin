package io.logto.sdk.android.auth

import android.net.Uri

internal object LogtoAuthManager {
    // Note - We will add "SOCIAL_AUTH_SESSION" in the future
    private const val LOGTO_AUTH_SESSION = "LOGTO_AUTH_SESSION"

    private val delegations = mutableMapOf<String, AuthSession>()

    fun handleAuthStart(authSession: AuthSession) {
        delegations[LOGTO_AUTH_SESSION] = authSession
    }

    fun handleCallbackUri(uri: Uri) {
        delegations.remove(LOGTO_AUTH_SESSION)?.handleCallbackUri(uri)
    }

    fun handleUserCancel() {
        delegations.remove(LOGTO_AUTH_SESSION)?.handleUserCancel()
    }

    fun isCurrentAuthingResult(redirectUri: String): Boolean {
        val sessionRedirectUri = delegations[LOGTO_AUTH_SESSION]?.redirectUri
        return sessionRedirectUri?.let {
            return redirectUri.startsWith(it)
        } ?: false
    }
}
