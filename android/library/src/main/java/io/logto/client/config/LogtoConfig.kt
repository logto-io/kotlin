package io.logto.client.config

import java.net.URLEncoder

data class LogtoConfig(
    val domain: String,
    val clientId: String,
    val scopes: List<String>,
    val redirectUri: String,
    val postLogoutRedirectUri: String,
) {
    val encodedScopes: String = scopes.joinToString(" ")

    val cacheKey: String = URLEncoder.encode("$clientId::$encodedScopes", "utf-8")

    private fun validate() {
        require(domain.isNotEmpty()) { "LogtoConfig: domain should not be empty" }
        require(clientId.isNotEmpty()) { "LogtoConfig: clientId should not be empty" }
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
