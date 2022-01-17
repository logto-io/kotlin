package io.logto.sdk.core.type

data class IdTokenClaims(
    val sub: String,
    val atHash: String,
    val aud: String,
    val exp: String,
    val iat: String,
    val iss: String,
)
