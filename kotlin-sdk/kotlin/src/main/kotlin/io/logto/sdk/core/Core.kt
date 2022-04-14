package io.logto.sdk.core

import io.logto.sdk.core.constant.CodeChallengeMethod
import io.logto.sdk.core.constant.GrantType
import io.logto.sdk.core.constant.PromptValue
import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.constant.ResponseType
import io.logto.sdk.core.exception.UriConstructionException
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.http.HttpEmptyCompletion
import io.logto.sdk.core.http.httpGet
import io.logto.sdk.core.http.httpPost
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.type.UserInfoResponse
import io.logto.sdk.core.util.ScopeUtils
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object Core {
    @Suppress("LongParameterList")
    fun generateSignInUri(
        authorizationEndpoint: String,
        clientId: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
        scopes: List<String>?,
        resources: List<String>?,
    ): String {
        val constructedUri = authorizationEndpoint.toHttpUrlOrNull() ?: throw UriConstructionException(
            UriConstructionException.Message.INVALID_ENDPOINT,
        )
        return constructedUri.newBuilder().apply {
            addQueryParameter(QueryKey.CLIENT_ID, clientId)
            addQueryParameter(QueryKey.CODE_CHALLENGE, codeChallenge)
            addQueryParameter(QueryKey.CODE_CHALLENGE_METHOD, CodeChallengeMethod.S256)
            addQueryParameter(QueryKey.STATE, state)
            addQueryParameter(QueryKey.REDIRECT_URI, redirectUri)
            addQueryParameter(QueryKey.PROMPT, PromptValue.CONSENT)
            addQueryParameter(QueryKey.RESPONSE_TYPE, ResponseType.CODE)
            addQueryParameter(QueryKey.SCOPE, ScopeUtils.withReservedScopes(scopes).joinToString(" "))
            resources?.let { for (value in it) { addQueryParameter(QueryKey.RESOURCE, value) } }
        }.build().toString()
    }

    fun generateSignOutUri(
        endSessionEndpoint: String,
        idToken: String,
        postLogoutRedirectUri: String? = null,
    ): String {
        val constructedUri = endSessionEndpoint.toHttpUrlOrNull() ?: throw UriConstructionException(
            UriConstructionException.Message.INVALID_ENDPOINT,
        )
        return constructedUri.newBuilder().apply {
            addQueryParameter(QueryKey.ID_TOKEN_HINT, idToken)
            postLogoutRedirectUri?.let {
                addQueryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI, postLogoutRedirectUri)
            }
        }.build().toString()
    }

    fun fetchOidcConfig(endpoint: String, completion: HttpCompletion<OidcConfigResponse>) =
        httpGet(endpoint, completion)

    @Suppress("LongParameterList")
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

    @Suppress("LongParameterList")
    fun fetchTokenByRefreshToken(
        tokenEndpoint: String,
        clientId: String,
        refreshToken: String,
        resource: String?,
        scopes: List<String>?,
        completion: HttpCompletion<RefreshTokenTokenResponse>,
    ) {
        val formBody = FormBody.Builder().apply {
            add(QueryKey.CLIENT_ID, clientId)
            add(QueryKey.REFRESH_TOKEN, refreshToken)
            add(QueryKey.GRANT_TYPE, GrantType.REFRESH_TOKEN)
            resource?.let { add(QueryKey.RESOURCE, it) }
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
