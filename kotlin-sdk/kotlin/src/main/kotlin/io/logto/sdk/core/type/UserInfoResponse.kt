package io.logto.sdk.core.type

import com.google.gson.JsonObject

data class UserInfoResponse(
    val sub: String,

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

    // Scope `custom_data`
    val customData: JsonObject?,

    // Scope `identities`
    val identities: JsonObject?,
)
