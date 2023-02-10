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
    val picture: String?,

    // Scope `email`
    val email: String?,
    val emailVerified: Boolean?,

    // Scope `phone`
    val phoneNumber: String?,
    val phoneNumberVerified: Boolean?,
)
