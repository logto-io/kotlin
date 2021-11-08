package io.logto.android.utils

import android.net.Uri

object Utils {
    fun buildUriWithQueries(baseUrl: String, parameters: Map<String, String>): Uri {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }
        return uriBuilder.build()
    }
}
