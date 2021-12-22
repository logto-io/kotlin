package io.logto.android.utils

import android.net.Uri
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException

object Utils {
    fun buildUriWithQueries(baseUrl: String, parameters: Map<String, String>): Uri {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }
        return uriBuilder.build()
    }

    fun validateRedirectUri(uri: Uri, baseUri: String): String? {
        if (uri.toString().isEmpty()) {
            return LogtoException.EMPTY_REDIRECT_URI
        }

        val errorDescription = uri.getQueryParameter(QueryKey.ERROR_DESCRIPTION)
        if (errorDescription != null) {
            return errorDescription
        }

        val error = uri.getQueryParameter(QueryKey.ERROR)
        if (error != null) {
            return error
        }

        if (!uri.toString().startsWith(baseUri)) {
            return LogtoException.INVALID_REDIRECT_URI
        }

        return null
    }
}
