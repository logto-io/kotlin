package io.logto.sdk.core.type

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
    val expiresIn: Long,
)
