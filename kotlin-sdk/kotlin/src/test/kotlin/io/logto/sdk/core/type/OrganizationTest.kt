package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OrganizationTest {
    private val id = "organization_id"
    private val name = "organization_name"
    private val description = "organization_description"

    private val organization = Organization(
        id = id,
        name = name,
        description = description,
    )

    @Test
    fun `IdTokenClaims should get expected content`() {
        assertThat(organization.id).isEqualTo(id)
        assertThat(organization.name).isEqualTo(name)
        assertThat(organization.description).isEqualTo(description)
    }
}
