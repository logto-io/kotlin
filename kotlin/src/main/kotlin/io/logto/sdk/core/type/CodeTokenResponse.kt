package io.logto.sdk.core.type

data class CodeTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val idToken: String,
    val scope: String,
    val expiresIn: Long,
)
