package io.logto.android.model

data class TokenSetParameters(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
)
