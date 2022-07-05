package io.logto.sdk.core.type

data class IdTokenClaims(
    val iss: String,
    val sub: String,
    val aud: String,
    val exp: Long,
    val iat: Long,
    val atHash: String?,

    // Scope `profile`
    val name: String?,
    val username: String?,
    val avatar: String?,
    val roleNames: List<String>?,
)
