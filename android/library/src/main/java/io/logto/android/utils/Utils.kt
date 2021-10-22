package io.logto.android.utils

import android.net.Uri
import io.logto.android.model.TokenSet
import kotlin.math.floor

object Utils {
    fun appendQueryParameters(uriBuilder: Uri.Builder, parameters: Map<String, String>): Uri {
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }
        return uriBuilder.build()
    }

    fun expiresAt(tokenSet: TokenSet): Long {
        return nowRoundToSec() + tokenSet.expiresIn
    }

    fun nowRoundToSec() = floor((System.currentTimeMillis() / 1000L).toDouble()).toLong()
}
