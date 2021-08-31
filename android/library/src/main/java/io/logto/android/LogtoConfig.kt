package io.logto.android

data class LogtoConfig(
    val clientId: String,
    val oidcEndpoint: String,
    val scopes: List<String>,
    val redirectUri: String,
) {

    fun getEncodedScopes(): String = scopes.joinToString(" ")

    fun getAuthEndpoint(): String = "$oidcEndpoint$AUTH_PATH"

    fun getTokenEndpoint(): String = "$oidcEndpoint$TOKEN_PATH"

    private companion object {
        const val AUTH_PATH = "auth"
        const val TOKEN_PATH = "token"
    }
}
