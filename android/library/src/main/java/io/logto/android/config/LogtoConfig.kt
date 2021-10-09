package io.logto.android.config

data class LogtoConfig(
    val clientId: String,
    val oidcEndpoint: String,
    val scopes: List<String>,
    val redirectUri: String,
    val postLogoutRedirectUri: String,
) {
    val encodedScopes: String
        get() = scopes.joinToString(" ")

    val authEndpoint: String
        get() = "$oidcEndpoint$AUTH_PATH"

    val tokenEndpoint: String
        get() = "$oidcEndpoint$TOKEN_PATH"

    val signOutEndpoint: String
        get() = "$oidcEndpoint$SIGN_OUT_PATH"

    private companion object {
        const val AUTH_PATH = "auth"
        const val TOKEN_PATH = "token"
        const val SIGN_OUT_PATH = "session/end"
    }
}
