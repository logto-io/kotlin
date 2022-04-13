package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OidcConfigResponseTest {

    private val authorizationEndpoint = "authorizationEndpoint"
    private val tokenEndpoint = "tokenEndpoint"
    private val endSessionEndpoint = "endSessionEndpoint"
    private val userinfoEndpoint = "userinfoEndpoint"
    private val jwksUri = "jwksUri"
    private val issuer = "issuer"
    private val revocationEndpoint = "revocationEndpoint"

    private val oidcConfigResponse = OidcConfigResponse(
        authorizationEndpoint = "authorizationEndpoint",
        tokenEndpoint = "tokenEndpoint",
        endSessionEndpoint = "endSessionEndpoint",
        userinfoEndpoint = "userinfoEndpoint",
        jwksUri = "jwksUri",
        issuer = "issuer",
        revocationEndpoint = "revocationEndpoint"
    )

    @Test
    fun getAuthorizationEndpoint() {
        assertThat(oidcConfigResponse.authorizationEndpoint).isEqualTo(authorizationEndpoint)
    }

    @Test
    fun getTokenEndpoint() {
        assertThat(oidcConfigResponse.tokenEndpoint).isEqualTo(tokenEndpoint)
    }

    @Test
    fun getEndSessionEndpoint() {
        assertThat(oidcConfigResponse.endSessionEndpoint).isEqualTo(endSessionEndpoint)
    }

    @Test
    fun getUserinfoEndpoint() {
        assertThat(oidcConfigResponse.userinfoEndpoint).isEqualTo(userinfoEndpoint)
    }

    @Test
    fun getJwksUri() {
        assertThat(oidcConfigResponse.jwksUri).isEqualTo(jwksUri)
    }

    @Test
    fun getIssuer() {
        assertThat(oidcConfigResponse.issuer).isEqualTo(issuer)
    }

    @Test
    fun getRevocationEndpoint() {
        assertThat(oidcConfigResponse.revocationEndpoint).isEqualTo(revocationEndpoint)
    }
}
