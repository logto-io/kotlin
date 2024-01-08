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

    // Scope `roles`
    val roles: List<String>?,

    // Scope `urn:logto:scope:organizations`
    val organizations: List<String>?,

    // Scope `urn:logto:scope:organization_roles`
    val organizationRoles: List<String>?,

    // Scope `urn:logto:scope:organizations`
    val organizationData: List<Organization>?,
)
