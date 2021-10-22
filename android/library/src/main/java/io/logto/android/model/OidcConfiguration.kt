package io.logto.android.model

data class OidcConfiguration(
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val endSessionEndpoint: String,
    val jwksUri: String,
    val issuer: String,
    val revocationEndpoint: String,
)
