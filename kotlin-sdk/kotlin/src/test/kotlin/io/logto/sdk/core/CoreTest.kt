package io.logto.sdk.core

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.*
import io.logto.sdk.core.exception.UriConstructionException
import io.logto.sdk.core.type.GenerateSignInUriOptions
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert
import org.junit.Test

class CoreTest {
    private val testAuthorizationEndpoint = "https://logto.dev/oidc/auth"
    private val testClientId = "clientId"
    private val testRedirectUri = "https://myapp.com/callback"
    private val testCodeChallenge = "codeChallenge"
    private val testState = "state"
    private val testScopes = listOf(
        ReservedScope.OPENID,
        ReservedScope.OFFLINE_ACCESS,
        UserScope.PROFILE,
    )
    private val testResourceVal1 = "api1.logto.dev"
    private val testResourceVal2 = "api2.logto.dev"
    private val testResources = listOf(testResourceVal1, testResourceVal2)
    private val testPromptValue = PromptValue.CONSENT

    @Test
    fun `generateSignInUri should contain expected queries in result`() {
        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = testScopes,
                resources = testResources,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            assertThat(scheme).isEqualTo(testAuthorizationEndpoint.toHttpUrl().scheme)
            assertThat(host).isEqualTo(testAuthorizationEndpoint.toHttpUrl().host)
            assertThat(pathSegments).isEqualTo(testAuthorizationEndpoint.toHttpUrl().pathSegments)
            assertThat(queryParameter(QueryKey.CLIENT_ID)).isEqualTo(testClientId)
            assertThat(queryParameter(QueryKey.CODE_CHALLENGE)).isEqualTo(testCodeChallenge)
            assertThat(queryParameter(QueryKey.CODE_CHALLENGE_METHOD)).isEqualTo(CodeChallengeMethod.S256)
            assertThat(queryParameter(QueryKey.STATE)).isEqualTo(testState)
            assertThat(queryParameter(QueryKey.REDIRECT_URI)).isEqualTo(testRedirectUri)
            assertThat(queryParameter(QueryKey.PROMPT)).isEqualTo(PromptValue.CONSENT)
            assertThat(queryParameter(QueryKey.RESPONSE_TYPE)).isEqualTo(ResponseType.CODE)
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(UserScope.PROFILE)
            }
            assertThat(queryParameterValues(QueryKey.RESOURCE)).apply {
                contains(testResourceVal1)
                contains(testResourceVal2)
            }
        }
    }

    @Test
    fun `generateSignInUri should contain not only reserved scopes but also the extra scope`() {
        val extraScope = "extraScope"
        val scopes = listOf(
            ReservedScope.OPENID,
            ReservedScope.OFFLINE_ACCESS,
            UserScope.PROFILE,
            extraScope
        )

        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = scopes,
                resources = testResources,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(UserScope.PROFILE)
                contains(extraScope)
            }
        }
    }

    @Test
    fun `generateSignInUri should not contain resources if no resources is provided`() {
        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = testScopes,
                resources = null,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            queryParameter(QueryKey.RESOURCE).isNullOrEmpty()
            queryParameterValues(QueryKey.RESOURCE).isEmpty()
        }
    }

    @Test
    fun `generateSignInUri should throw exception if the authorization endpoint is invalid`() {
        val authorizationEndpoint = "invalid_endpoint"

        val expectedException = Assert.assertThrows(UriConstructionException::class.java) {
            Core.generateSignInUri(
                GenerateSignInUriOptions(
                    authorizationEndpoint = authorizationEndpoint,
                    clientId = testClientId,
                    redirectUri = testRedirectUri,
                    codeChallenge = testCodeChallenge,
                    state = testState,
                    scopes = testScopes,
                    resources = testResources,
                    prompt = testPromptValue,
                ),
            )
        }

        assertThat(expectedException).hasMessageThat()
            .contains(UriConstructionException.Type.INVALID_ENDPOINT.name)
    }

    @Test
    fun `generateSignInUri should always contain reserved scopes even if no scope is provided`() {
        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = null,
                resources = testResources,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(UserScope.PROFILE)
            }
        }
    }

    @Test
    fun `generateSignInUri should always contain reserved scopes if only the reserved OFFLINE_ACCESS is provided`() {
        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = listOf(ReservedScope.OFFLINE_ACCESS),
                resources = testResources,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(UserScope.PROFILE)
            }
        }
    }

    @Test
    fun `generateSignInUri should always contain reserved scopes if only the reserved OPENID is provided`() {

        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = listOf(ReservedScope.OPENID),
                resources = testResources,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(UserScope.PROFILE)
            }
        }
    }

    @Test
    fun `generateSignInUri should always contain reserved scopes if only the reserved PROFILE is provided`() {

        val signInUri = Core.generateSignInUri(
            GenerateSignInUriOptions(
                authorizationEndpoint = testAuthorizationEndpoint,
                clientId = testClientId,
                redirectUri = testRedirectUri,
                codeChallenge = testCodeChallenge,
                state = testState,
                scopes = listOf(UserScope.PROFILE),
                resources = testResources,
                prompt = testPromptValue,
            ),
        )

        signInUri.toHttpUrl().apply {
            assertThat(queryParameter(QueryKey.SCOPE)).apply {
                contains(ReservedScope.OPENID)
                contains(ReservedScope.OFFLINE_ACCESS)
                contains(UserScope.PROFILE)
            }
        }
    }

    @Test
    fun `generateSignOutUri should contain expected queries`() {
        val endSessionEndpoint = "https://logto.dev/oidc/endSession"
        val clientId = "clientId"
        val postLogoutRedirectUri = "https://myapp.com/logout_callback"

        val resultUri = Core.generateSignOutUri(endSessionEndpoint, clientId, postLogoutRedirectUri)

        val constructedUri = resultUri.toHttpUrl()
        assertThat(constructedUri.scheme).isEqualTo(endSessionEndpoint.toHttpUrl().scheme)
        assertThat(constructedUri.host).isEqualTo(endSessionEndpoint.toHttpUrl().host)
        assertThat(constructedUri.pathSegments).isEqualTo(endSessionEndpoint.toHttpUrl().pathSegments)
        assertThat(constructedUri.queryParameter(QueryKey.CLIENT_ID)).isEqualTo(clientId)
        assertThat(constructedUri.queryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI)).isEqualTo(
            postLogoutRedirectUri
        )
    }

    @Test
    fun `generateSignOutUri should not contain postLogoutRedirectUri if that is not provided`() {
        val endSessionEndpoint = "https://logto.dev/oidc/endSession"
        val clientId = "clientId"

        val resultUri = Core.generateSignOutUri(endSessionEndpoint, clientId)

        val constructedUri = resultUri.toHttpUrl()
        assertThat(constructedUri.scheme).isEqualTo(endSessionEndpoint.toHttpUrl().scheme)
        assertThat(constructedUri.host).isEqualTo(endSessionEndpoint.toHttpUrl().host)
        assertThat(constructedUri.pathSegments).isEqualTo(endSessionEndpoint.toHttpUrl().pathSegments)
        assertThat(constructedUri.queryParameter(QueryKey.CLIENT_ID)).isEqualTo(clientId)
        assertThat(constructedUri.queryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI)).isEqualTo(null)
    }

    @Test
    fun `generateSignOutUri should throw exception if the endSessionEndpoint is invalid`() {
        val endSessionEndpoint = "invalid_endpoint"
        val clientId = "clientId"

        val expectedException = Assert.assertThrows(UriConstructionException::class.java) {
            Core.generateSignOutUri(endSessionEndpoint, clientId)
        }

        assertThat(expectedException).hasMessageThat()
            .contains(UriConstructionException.Type.INVALID_ENDPOINT.name)
    }
}
