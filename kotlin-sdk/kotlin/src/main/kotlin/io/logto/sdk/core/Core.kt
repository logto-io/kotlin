package io.logto.sdk.core

import io.logto.sdk.core.constant.CodeChallengeMethod
import io.logto.sdk.core.constant.GrantType
import io.logto.sdk.core.constant.PromptValue
import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.constant.ReservedResource
import io.logto.sdk.core.constant.ResponseType
import io.logto.sdk.core.constant.UserScope
import io.logto.sdk.core.exception.UriConstructionException
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.http.HttpEmptyCompletion
import io.logto.sdk.core.http.httpGet
import io.logto.sdk.core.http.httpPost
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.GenerateSignInUriOptions
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.type.UserInfoResponse
import io.logto.sdk.core.util.ScopeUtils
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object Core {
    fun generateSignInUri(options: GenerateSignInUriOptions): String {
        val constructedUri = options.authorizationEndpoint.toHttpUrlOrNull() ?: throw UriConstructionException(
            UriConstructionException.Type.INVALID_ENDPOINT,
        )

        return constructedUri.newBuilder().apply {
            addQueryParameter(QueryKey.CLIENT_ID, options.clientId)
            addQueryParameter(QueryKey.CODE_CHALLENGE, options.codeChallenge)
            addQueryParameter(QueryKey.CODE_CHALLENGE_METHOD, CodeChallengeMethod.S256)
            addQueryParameter(QueryKey.STATE, options.state)
            addQueryParameter(QueryKey.REDIRECT_URI, options.redirectUri)
            addQueryParameter(QueryKey.RESPONSE_TYPE, ResponseType.CODE)

            val usedScopes = ScopeUtils.withDefaultScopes(options.scopes)
            addQueryParameter(QueryKey.SCOPE, usedScopes.joinToString(" "))

            val usedResources = options.resources.orEmpty()
            for (value in usedResources) { addQueryParameter(QueryKey.RESOURCE, value) }
            if (
                usedScopes.contains(UserScope.ORGANIZATIONS) &&
                !usedResources.contains(ReservedResource.ORGANIZATION)
            ) {
                addQueryParameter(QueryKey.RESOURCE, ReservedResource.ORGANIZATION)
            }

            addQueryParameter(QueryKey.PROMPT, options.prompt ?: PromptValue.CONSENT)
        }.build().toString()
    }

    fun generateSignOutUri(
        endSessionEndpoint: String,
        clientId: String,
        postLogoutRedirectUri: String? = null,
    ): String {
        val constructedUri = endSessionEndpoint.toHttpUrlOrNull() ?: throw UriConstructionException(
            UriConstructionException.Type.INVALID_ENDPOINT,
        )
        return constructedUri.newBuilder().apply {
            addQueryParameter(QueryKey.CLIENT_ID, clientId)
            postLogoutRedirectUri?.let {
                addQueryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI, postLogoutRedirectUri)
            }
        }.build().toString()
    }

    fun fetchOidcConfig(endpoint: String, completion: HttpCompletion<OidcConfigResponse>) =
        httpGet(endpoint, completion)

    fun fetchJwksJson(jwksUri: String, completion: HttpCompletion<String>) =
        httpGet(jwksUri, completion)

    fun fetchTokenByAuthorizationCode(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        codeVerifier: String,
        code: String,
        resource: String?,
        completion: HttpCompletion<CodeTokenResponse>,
    ) {
        val formBody = FormBody.Builder().apply {
            add(QueryKey.CLIENT_ID, clientId)
            add(QueryKey.REDIRECT_URI, redirectUri)
            add(QueryKey.CODE_VERIFIER, codeVerifier)
            add(QueryKey.CODE, code)
            add(QueryKey.GRANT_TYPE, GrantType.AUTHORIZATION_CODE)
            resource?.let { add(QueryKey.RESOURCE, it) }
        }.build()
        httpPost(tokenEndpoint, formBody, completion)
    }

    fun fetchTokenByRefreshToken(
        /** The token endpoint of the authorization server. */
        tokenEndpoint: String,
        /** The client ID of the application. */
        clientId: String,
        /** The refresh token to be used to fetch the access token. */
        refreshToken: String,
        /** The API resource to be fetch the access token for. */
        resource: String?,
        /** The ID of the organization to be fetch the access token for. */
        organizationId: String?,
        /**
         * The scopes to request for the access token. If not provided, the authorization server
         * will use all the scopes that the client is authorized for.
         */
        scopes: List<String>?,
        completion: HttpCompletion<RefreshTokenTokenResponse>,
    ) {
        val formBody = FormBody.Builder().apply {
            add(QueryKey.CLIENT_ID, clientId)
            add(QueryKey.REFRESH_TOKEN, refreshToken)
            add(QueryKey.GRANT_TYPE, GrantType.REFRESH_TOKEN)
            resource?.let { add(QueryKey.RESOURCE, it) }
            organizationId?.let { add(QueryKey.ORGANIZATION_ID, it) }
            scopes?.let { add(QueryKey.SCOPE, it.joinToString(" ")) }
        }.build()
        httpPost(tokenEndpoint, formBody, completion)
    }

    fun fetchUserInfo(
        userInfoEndpoint: String,
        accessToken: String,
        completion: HttpCompletion<UserInfoResponse>,
    ) = httpGet(
        userInfoEndpoint,
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        completion,
    )

    fun revoke(
        revocationEndpoint: String,
        clientId: String,
        token: String,
        completion: HttpEmptyCompletion,
    ) {
        val formBody = FormBody.Builder().apply {
            add(QueryKey.CLIENT_ID, clientId)
            add(QueryKey.TOKEN, token)
        }.build()
        httpPost(revocationEndpoint, formBody, completion)
    }
}
