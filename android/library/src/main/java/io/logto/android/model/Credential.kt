package io.logto.android.model

data class Credential(
    val accessToken: String,
    val expiresIn: String,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
)
