package io.logto.sdk.core.type

data class OidcConfigResponse(
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val endSessionEndpoint: String,
    val jwksUri: String,
    val issuer: String,
    val revocationEndpoint: String,
)
