package io.logto.sdk.core.type

data class IdTokenClaims(
    /** Issuer of this token. */
    val iss: String,
    /** Subject (the user ID) of this token. */
    val sub: String,
    /** Audience (the client ID) of this token. */
    val aud: String,
    /** Expiration time of this token. */
    val exp: Long,
    /** Time at which this token was issued. */
    val iat: Long,
    val atHash: String?,

    // Scope `profile`
    /** Full name of the user. */
    val name: String?,
    /** Username of the user. */
    val username: String?,
    /** URL of the user's profile picture. */
    val picture: String?,

    // Scope `email`
    /** Email address of the user. */
    val email: String?,
    /** Whether the user's email address has been verified. */
    val emailVerified: Boolean?,

    // Scope `phone`
    /** Phone number of the user. */
    val phoneNumber: String?,
    /** Whether the user's phone number has been verified. */
    val phoneNumberVerified: Boolean?,

    // Scope `roles`
    /** Roles that the user has for API resources. */
    val roles: List<String>?,

    // Scope `urn:logto:scope:organizations`
    /** Organization IDs that the user has membership in. */
    val organizations: List<String>?,

    // Scope `urn:logto:scope:organization_roles`
    /**
     * All organization roles that the user has. The format is `{organizationId}:{roleName}`.
     *
     * Note that not all organizations are included in this list, only the ones that the user has roles in.
     *
     * @example
     * ```ts
     * ['org1:admin', 'org2:member'] // The user is an admin of org1 and a member of org2.
     * ```
     */
    val organizationRoles: List<String>?,
)
