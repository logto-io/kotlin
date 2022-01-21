package io.logto.sdk.core

import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.exception.UriConstructionException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object Core {
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
