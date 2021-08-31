package io.logto.android.utils

import android.net.Uri

class UrlUtil {
    companion object {
        fun getQueryParam(url: String, key: String) = Uri.parse(url).getQueryParameter(key)

        fun appendQueryParameters(uriBuilder: Uri.Builder, parameters: Map<String, String>): Uri {
            for ((key, value) in parameters) {
                uriBuilder.appendQueryParameter(key, value)
            }
            return uriBuilder.build()
        }
    }
}
