package io.logto.android.config

data class LogtoConfig(
    val logtoUrl: String,
    val clientId: String,
    val scopes: List<String>,
    val redirectUri: String,
    val postLogoutRedirectUri: String,
) {
    val encodedScopes: String
        get() = scopes.joinToString(" ")

    private fun validate() {
        require(logtoUrl.isNotEmpty()) { "LogtoConfig: logtoUrl should not be empty"}
        require(clientId.isNotEmpty()) { "LogtoConfig: clientId should not be empty"}
        require(scopes.isNotEmpty()) { "LogtoConfig: scope list should not be empty" }
        require(redirectUri.isNotEmpty()) { "LogtoConfig: redirectUri should not be empty" }
        require(postLogoutRedirectUri.isNotEmpty()) {
            "LogtoConfig: postLogoutRedirectUri should not be empty"
        }
    }

    init {
        validate()
    }
}
