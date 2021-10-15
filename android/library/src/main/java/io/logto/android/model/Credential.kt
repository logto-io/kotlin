package io.logto.android.model

data class Credential(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
)
