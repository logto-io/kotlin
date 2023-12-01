package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import org.junit.Test

class UserInfoResponseTest {
    private val sub = "testSub"
    private val name = "name"
    private val username = "username"
    private val picture = "picture"
    private val email = "email"
    private val emailVerified = true
    private val phoneNumber = "123456789"
    private val phoneNumberVerified = true
    private val customData = JsonObject()
    private val identities = JsonObject()
    private val organizationData = listOf(
        Organization("org_id", "org_name", "org_desc"),
    )

    private val userInfoResponse = UserInfoResponse(
        sub = sub,
        name = name,
        username = username,
        picture = picture,
        email = email,
        emailVerified = emailVerified,
        phoneNumber = phoneNumber,
        phoneNumberVerified = phoneNumberVerified,
        customData = customData,
        identities = identities,
        organizationData = organizationData,
    )

    @Test
    fun `RefreshTokenTokenResponse should get expected content`() {
        assertThat(userInfoResponse.sub).isEqualTo(sub)
        assertThat(userInfoResponse.name).isEqualTo(name)
        assertThat(userInfoResponse.username).isEqualTo(username)
        assertThat(userInfoResponse.picture).isEqualTo(picture)
        assertThat(userInfoResponse.email).isEqualTo(email)
        assertThat(userInfoResponse.emailVerified).isEqualTo(emailVerified)
        assertThat(userInfoResponse.phoneNumber).isEqualTo(phoneNumber)
        assertThat(userInfoResponse.phoneNumberVerified).isEqualTo(phoneNumberVerified)
        assertThat(userInfoResponse.customData).isEqualTo(customData)
        assertThat(userInfoResponse.identities).isEqualTo(identities)
        assertThat(userInfoResponse.organizationData).isEqualTo(organizationData)
    }
}
