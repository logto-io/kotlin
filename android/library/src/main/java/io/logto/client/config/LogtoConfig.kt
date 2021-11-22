package io.logto.client.config

data class LogtoConfig(
    val domain: String,
    val clientId: String,
    val scopeValues: List<String>,
    val redirectUri: String,
    val postLogoutRedirectUri: String,
) {
    val scope: String = scopeValues.joinToString(" ")

    val cacheKey: String = "$clientId::$scope"

    private fun validate() {
        require(domain.isNotEmpty()) { "LogtoConfig: domain should not be empty" }
        require(clientId.isNotEmpty()) { "LogtoConfig: clientId should not be empty" }
        require(scopeValues.isNotEmpty()) { "LogtoConfig: scopeValues should not be empty" }
        require(redirectUri.isNotEmpty()) { "LogtoConfig: redirectUri should not be empty" }
        require(postLogoutRedirectUri.isNotEmpty()) {
            "LogtoConfig: postLogoutRedirectUri should not be empty"
        }
    }

    init {
        validate()
    }
}
