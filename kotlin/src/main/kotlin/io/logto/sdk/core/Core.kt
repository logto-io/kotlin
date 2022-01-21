package io.logto.sdk.core

import io.logto.sdk.core.constant.CodeChallengeMethod
import io.logto.sdk.core.constant.PromptValue
import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.constant.ResponseType
import io.logto.sdk.core.exception.UriConstructionException
import io.logto.sdk.core.extension.ensureDefaultScopes
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

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
}
