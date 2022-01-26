package io.logto.sdk.core

import com.google.gson.JsonObject
import io.logto.sdk.core.constant.CodeChallengeMethod
import io.logto.sdk.core.constant.GrantType
import io.logto.sdk.core.constant.MediaType
import io.logto.sdk.core.constant.PromptValue
import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.constant.ResponseType
import io.logto.sdk.core.exception.UriConstructionException
import io.logto.sdk.core.extension.ensureDefaultScopes
import io.logto.sdk.core.http.completion.HttpCompletion
import io.logto.sdk.core.http.completion.HttpEmptyCompletion
import io.logto.sdk.core.http.httpGet
import io.logto.sdk.core.http.httpPost
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.type.UserInfoResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object Core {
    @Suppress("LongParameterList")
    fun generateSignInUri(
        authorizationEndpoint: String,
        clientId: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
        scope: List<String>?,
        resource: List<String>?,
    ): String {
        val constructedUri = authorizationEndpoint.toHttpUrlOrNull() ?: throw UriConstructionException(
            UriConstructionException.Message.INVALID_ENDPOINT
        )
        return constructedUri.newBuilder().apply {
            addQueryParameter(QueryKey.CLIENT_ID, clientId)
            addQueryParameter(QueryKey.CODE_CHALLENGE, codeChallenge)
            addQueryParameter(QueryKey.CODE_CHALLENGE_METHOD, CodeChallengeMethod.S256)
            addQueryParameter(QueryKey.STATE, state)
            addQueryParameter(QueryKey.REDIRECT_URI, redirectUri)
            addQueryParameter(QueryKey.PROMPT, PromptValue.CONSENT)
            addQueryParameter(QueryKey.RESPONSE_TYPE, ResponseType.CODE)
            addQueryParameter(QueryKey.SCOPE, scope.ensureDefaultScopes().joinToString(" "))
            resource?.let { for (value in it) { addQueryParameter(QueryKey.RESOURCE, value) } }
        }.build().toString()
    }

    fun generateSignOutUri(
        endSessionEndpoint: String,
        idToken: String,
        postLogoutRedirectUri: String? = null
    ): String {
        val constructedUri = endSessionEndpoint.toHttpUrlOrNull() ?: throw UriConstructionException(
            UriConstructionException.Message.INVALID_ENDPOINT
        )
        return constructedUri.newBuilder().apply {
            addQueryParameter(QueryKey.ID_TOKEN_HINT, idToken)
            postLogoutRedirectUri?.let {
                addQueryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI, postLogoutRedirectUri)
            }
        }.build().toString()
    }

    fun fetchOidConfig(endpoint: String, completion: HttpCompletion<OidcConfigResponse>) =
        httpGet(endpoint, completion)

    @Suppress("LongParameterList")
    fun fetchTokenByAuthorizationCode(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        codeVerifier: String,
        code: String,
        resource: String?,
        completion: HttpCompletion<CodeTokenResponse>
    ) {
        val body = JsonObject().apply {
            addProperty(QueryKey.CLIENT_ID, clientId)
            addProperty(QueryKey.REDIRECT_URI, redirectUri)
            addProperty(QueryKey.CODE_VERIFIER, codeVerifier)
            addProperty(QueryKey.CODE, code)
            addProperty(QueryKey.GRANT_TYPE, GrantType.AUTHORIZATION_CODE)
            resource?.let { addProperty(QueryKey.RESOURCE, it) }
        }.toString().toRequestBody(MediaType.X_WWW_FORM_URLENCODED.toMediaType())
        httpPost(tokenEndpoint, body, completion)
    }

    @Suppress("LongParameterList")
    fun fetchTokenByRefreshToken(
        tokenEndpoint: String,
        clientId: String,
        refreshToken: String,
        resource: String?,
        scope: List<String>?,
        completion: HttpCompletion<RefreshTokenTokenResponse>
    ) {
        val body = JsonObject().apply {
            addProperty(QueryKey.CLIENT_ID, clientId)
            addProperty(QueryKey.REFRESH_TOKEN, refreshToken)
            addProperty(QueryKey.GRANT_TYPE, GrantType.REFRESH_TOKEN)
            resource?.let { addProperty(QueryKey.RESOURCE, it) }
            scope?.let { addProperty(QueryKey.SCOPE, it.ensureDefaultScopes().joinToString(" ")) }
        }.toString().toRequestBody(MediaType.X_WWW_FORM_URLENCODED.toMediaType())
        httpPost(tokenEndpoint, body, completion)
    }

    fun fetchUserInfo(
        userInfoEndpoint: String,
        accessToken: String,
        completion: HttpCompletion<UserInfoResponse?>
    ) = httpGet(
        userInfoEndpoint,
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        completion
    )

    fun revoke(
        revocationEndpoint: String,
        clientId: String,
        token: String,
        completion: HttpEmptyCompletion
    ) {
        val body = JsonObject().apply {
            addProperty(QueryKey.CLIENT_ID, clientId)
            addProperty(QueryKey.TOKEN, token)
        }.toString().toRequestBody(MediaType.X_WWW_FORM_URLENCODED.toMediaType())
        httpPost(revocationEndpoint, body, completion)
    }
}
