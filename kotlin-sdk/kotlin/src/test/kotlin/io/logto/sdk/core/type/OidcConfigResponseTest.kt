package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OidcConfigResponseTest {

    private val authorizationEndpoint = "authorizationEndpoint"
    private val tokenEndpoint = "tokenEndpoint"
    private val endSessionEndpoint = "endSessionEndpoint"
    private val jwksUri = "jwksUri"
    private val issuer = "issuer"
    private val revocationEndpoint = "revocationEndpoint"

    private val oidcConfigResponse = OidcConfigResponse(
        authorizationEndpoint = "authorizationEndpoint",
        tokenEndpoint = "tokenEndpoint",
        endSessionEndpoint = "endSessionEndpoint",
        jwksUri = "jwksUri",
        issuer = "issuer",
        revocationEndpoint = "revocationEndpoint"
    )

    @Test
    fun `OidcConfigResponse should get expected authorization endpoint`() {
        assertThat(oidcConfigResponse.authorizationEndpoint).isEqualTo(authorizationEndpoint)
    }

    @Test
    fun `OidcConfigResponse should get expected token endpoint`() {
        assertThat(oidcConfigResponse.tokenEndpoint).isEqualTo(tokenEndpoint)
    }

    @Test
    fun `OidcConfigResponse should get expected end session endpoint`() {
        assertThat(oidcConfigResponse.endSessionEndpoint).isEqualTo(endSessionEndpoint)
    }

    @Test
    fun `OidcConfigResponse should get expected jwks URI`() {
        assertThat(oidcConfigResponse.jwksUri).isEqualTo(jwksUri)
    }

    @Test
    fun `OidcConfigResponse should get expected issuer`() {
        assertThat(oidcConfigResponse.issuer).isEqualTo(issuer)
    }

    @Test
    fun `OidcConfigResponse should get expected revocation endpoint`() {
        assertThat(oidcConfigResponse.revocationEndpoint).isEqualTo(revocationEndpoint)
    }
}
