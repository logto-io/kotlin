package io.logto.sdk.core.constant

object UserScope {
    /**
     * Scope for basic user info.
     */
    const val PROFILE = "profile"
    /**
     * Scope for user email address.
     */
    const val EMAIL = "email"
    /**
     * Scope for user phone number.
     */
    const val PHONE = "phone"
    /**
     * Scope for user's custom data.
     */
    const val CUSTOM_DATA = "custom_data"
    /**
     * Scope for user's social identity details.
     */
    const val IDENTITIES = "identities"
    /**
     * Scope for user's roles.
     */
    const val ROLES = "roles"
    /**
     * Scope for user's organization IDs and perform organization token
     * grant per [RFC 0001](https://github.com/logto-io/rfcs).
     */
    const val ORGANIZATIONS = "urn:logto:scope:organizations"
    /**
     * Scope for user's organization roles per [RFC 0001](https://github.com/logto-io/rfcs).
     */
    const val ORGANIZATION_ROLES = "urn:logto:scope:organization_roles"
}
