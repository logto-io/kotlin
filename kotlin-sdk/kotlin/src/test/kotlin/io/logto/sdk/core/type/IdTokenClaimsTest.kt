package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import org.junit.Test

class IdTokenClaimsTest {
    private val iss = "iss"
    private val sub = "sub"
    private val aud = "aud"
    private val exp = 60L
    private val iat = 1234L
    private val atHash = "atHash"
    private val name = "name"
    private val username = "username"
    private val picture = "picture"
    private val email = "email"
    private val emailVerified = true
    private val phoneNumber = "123456789"
    private val phoneNumberVerified = true
    private val roles = listOf("reader", "writer")
    private val organizations = listOf("silverhand", "logto")
    private val organizationRoles = listOf("designer", "engineer")

    private val idTokenClaims = IdTokenClaims(
        iss = iss,
        sub = sub,
        aud = aud,
        exp = exp,
        iat = iat,
        atHash = atHash,
        name = name,
        username = username,
        picture = picture,
        email = email,
        emailVerified = emailVerified,
        phoneNumber = phoneNumber,
        phoneNumberVerified = phoneNumberVerified,
        roles = roles,
        organizations = organizations,
        organizationRoles = organizationRoles,
    )

    @Test
    fun `IdTokenClaims should get expected content`() {
        assertThat(idTokenClaims.sub).isEqualTo(sub)
        assertThat(idTokenClaims.iss).isEqualTo(iss)
        assertThat(idTokenClaims.aud).isEqualTo(aud)
        assertThat(idTokenClaims.sub).isEqualTo(sub)
        assertThat(idTokenClaims.exp).isEqualTo(exp)
        assertThat(idTokenClaims.iat).isEqualTo(iat)
        assertThat(idTokenClaims.atHash).isEqualTo(atHash)
        assertThat(idTokenClaims.name).isEqualTo(name)
        assertThat(idTokenClaims.username).isEqualTo(username)
        assertThat(idTokenClaims.picture).isEqualTo(picture)
        assertThat(idTokenClaims.email).isEqualTo(email)
        assertThat(idTokenClaims.emailVerified).isEqualTo(emailVerified)
        assertThat(idTokenClaims.phoneNumber).isEqualTo(phoneNumber)
        assertThat(idTokenClaims.phoneNumberVerified).isEqualTo(phoneNumberVerified)
        assertThat(idTokenClaims.roles).isEqualTo(roles)
        assertThat(idTokenClaims.organizations).isEqualTo(organizations)
        assertThat(idTokenClaims.organizationRoles).isEqualTo(organizationRoles)
    }
}
