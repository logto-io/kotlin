package io.logto.sdk.core.type

data class IdTokenClaims(
    val iss: String,
    val sub: String,
    val aud: String,
    val exp: String,
    val iat: String,
    val atHash: String?,
)
