package io.logto.sdk.android.type

data class AccessToken(
    val token: String,
    val scope: String,
    val expiresAt: Long,
)
