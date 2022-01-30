package io.logto.sdk.core

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.constant.ReservedScope
import io.logto.sdk.core.exception.UriConstructionException
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert
import org.junit.Test

class CoreTest {
    private val testAuthorizationEndpoint = "https://logto.dev/oidc/auth"
    private val testClientId = "clientId"
    private val testRedirectUri = "https://myapp.com/callback"
    private val testCodeChallenge = "codeChallenge"
    private val testState = "state"
    private val testScopes = listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS)
    private val testResourceVal1 = "api1.logto.dev"
    private val testResourceVal2 = "api2.logto.dev"
    private val testResources = listOf(testResourceVal1, testResourceVal2)

    @Test
    fun generateSignInUri() {
        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = testAuthorizationEndpoint,
            clientId = testClientId,
            redirectUri = testRedirectUri,
            codeChallenge = testCodeChallenge,
            state = testState,
            scopes = testScopes,
            resources = testResources,
        )

        signInUri.toHttpUrl().apply {
            assertThat(scheme).isEqualTo(testAuthorizationEndpoint.toHttpUrl().scheme)
            assertThat(host).isEqualTo(testAuthorizationEndpoint.toHttpUrl().host)
            assertThat(pathSegments).isEqualTo(testAuthorizationEndpoint.toHttpUrl().pathSegments)
            assertThat(queryParameter(QueryKey.CLIENT_ID)).isEqualTo(testClientId)
            assertThat(queryParameter(QueryKey.REDIRECT_URI)).isEqualTo(testRedirectUri)
            assertThat(queryParameter(QueryKey.CODE_CHALLENGE)).isEqualTo(testCodeChallenge)
            assertThat(queryParameter(QueryKey.STATE)).isEqualTo(testState)
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
            }
            assertThat(queryParameterValues(QueryKey.RESOURCE)).apply {
                contains(testResourceVal1)
                contains(testResourceVal2)
            }
        }
    }

    @Test
    fun generateSignInUriShouldContainsExtraScope() {
        val extraScope = "extraScope"
        val scopes = listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS, extraScope)

        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = testAuthorizationEndpoint,
            clientId = testClientId,
            redirectUri = testRedirectUri,
            codeChallenge = testCodeChallenge,
            state = testState,
            scopes = scopes,
            resources = testResources,
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(extraScope)
            }
        }
    }

    @Test
    fun generateSignInUriWithoutResource() {
        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = testAuthorizationEndpoint,
            clientId = testClientId,
            redirectUri = testRedirectUri,
            codeChallenge = testCodeChallenge,
            state = testState,
            scopes = testScopes,
            resources = null,
        )

        signInUri.toHttpUrl().apply {
            queryParameter(QueryKey.RESOURCE).isNullOrEmpty()
            queryParameterValues(QueryKey.RESOURCE).isEmpty()
        }
    }

    @Test
    fun generateSignInUriWithInvalidAuthEndpointShouldThrow() {
        val authorizationEndpoint = "invalid_endpoint"

        val expectedException = Assert.assertThrows(UriConstructionException::class.java) {
            Core.generateSignInUri(
                authorizationEndpoint = authorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = testScopes,
                resources = testResources,
            )
        }

        assertThat(expectedException).hasMessageThat().contains(UriConstructionException.Message.INVALID_ENDPOINT.name)
    }

    @Test
    fun generateSignInUriWithoutScopeShouldContainsDefaultScopes() {
        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = testAuthorizationEndpoint,
            clientId = testClientId,
            redirectUri = testRedirectUri,
            codeChallenge = testCodeChallenge,
            state = testState,
            scopes = null,
            resources = testResources,
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
            }
        }
    }

    @Test
    fun generateSignInUriWithMissingOpenIdScopeShouldContainsDefaultScopes() {
        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = testAuthorizationEndpoint,
            clientId = testClientId,
            redirectUri = testRedirectUri,
            codeChallenge = testCodeChallenge,
            state = testState,
            scopes = listOf(ReservedScope.OFFLINE_ACCESS),
            resources = testResources,
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
            }
        }
    }

    @Test
    fun generateSignInUriWithMissingOfflineAccessScopeShouldContainsDefaultScopes() {

        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = testAuthorizationEndpoint,
            clientId = testClientId,
            redirectUri = testRedirectUri,
            codeChallenge = testCodeChallenge,
            state = testState,
            scopes = listOf(ReservedScope.OPENID),
            resources = testResources,
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
            }
        }
    }

    @Test
    fun generateSignOutUriWithBothIdTokenAndPostLogoutRedirectUri() {
        val endSessionEndpoint = "https://logto.dev/oidc/endSession"
        val idToken = "idToken"
        val postLogoutRedirectUri = "https://myapp.com/logout_callback"

        val resultUri = Core.generateSignOutUri(endSessionEndpoint, idToken, postLogoutRedirectUri)

        val constructedUri = resultUri.toHttpUrl()
        assertThat(constructedUri.scheme).isEqualTo(endSessionEndpoint.toHttpUrl().scheme)
        assertThat(constructedUri.host).isEqualTo(endSessionEndpoint.toHttpUrl().host)
        assertThat(constructedUri.pathSegments).isEqualTo(endSessionEndpoint.toHttpUrl().pathSegments)
        assertThat(constructedUri.queryParameter(QueryKey.ID_TOKEN_HINT)).isEqualTo(idToken)
        assertThat(constructedUri.queryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI)).isEqualTo(postLogoutRedirectUri)
    }

    @Test
    fun generateSignOutUriWithoutPostLogoutRedirectUri() {
        val endSessionEndpoint = "https://logto.dev/oidc/endSession"
        val idToken = "idToken"

        val resultUri = Core.generateSignOutUri(endSessionEndpoint, idToken)

        val constructedUri = resultUri.toHttpUrl()
        assertThat(constructedUri.scheme).isEqualTo(endSessionEndpoint.toHttpUrl().scheme)
        assertThat(constructedUri.host).isEqualTo(endSessionEndpoint.toHttpUrl().host)
        assertThat(constructedUri.pathSegments).isEqualTo(endSessionEndpoint.toHttpUrl().pathSegments)
        assertThat(constructedUri.queryParameter(QueryKey.ID_TOKEN_HINT)).isEqualTo(idToken)
        assertThat(constructedUri.queryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI)).isEqualTo(null)
    }

    @Test
    fun generateSignOutUriShouldThrowWithInvalidEndpoint() {
        val endSessionEndpoint = "invalid_endpoint"
        val idToken = "idToken"

        val expectedException = Assert.assertThrows(UriConstructionException::class.java) {
            Core.generateSignOutUri(endSessionEndpoint, idToken)
        }

        assertThat(expectedException).hasMessageThat().contains(UriConstructionException.Message.INVALID_ENDPOINT.name)
    }
}
