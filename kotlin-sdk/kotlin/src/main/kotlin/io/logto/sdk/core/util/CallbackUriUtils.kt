package io.logto.sdk.core.util

import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.exception.CallbackUriVerificationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object CallbackUriUtils {
    fun verifyAndParseCodeFromCallbackUri(
        callbackUri: String,
        redirectUri: String,
        state: String,
    ): String {
        // Note: Check scheme
        if (!callbackUri.startsWith(redirectUri)) {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.URI_MISMATCHED,
            )
        }

        // Note: Support custom scheme
        // TODO - LOG-1487: Replace HttpUrl with More Suitable Utils
        var validFormatUri = callbackUri
        if (!callbackUri.startsWith("http")) {
            if (!callbackUri.contains("://")) {
                throw CallbackUriVerificationException(CallbackUriVerificationException.Type.INVALID_URI_FORMAT)
            }
            validFormatUri = callbackUri.replaceBefore("://", "http")
        }

        val parsedUri = validFormatUri.toHttpUrlOrNull()
            ?: throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.INVALID_URI_FORMAT,
            )

        parsedUri.queryParameter(QueryKey.ERROR)?.let {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.ERROR_FOUND_IN_URI,
            ).apply {
                error = it
                errorDesc = parsedUri.queryParameter(QueryKey.ERROR_DESCRIPTION)
            }
        }

        parsedUri.queryParameter(QueryKey.STATE)?.let {
            if (it != state) {
                throw CallbackUriVerificationException(
                    CallbackUriVerificationException.Type.STATE_MISMATCHED,
                )
            }
        } ?: throw CallbackUriVerificationException(
            CallbackUriVerificationException.Type.MISSING_STATE_URI_PARAMETER,
        )

        return parsedUri.queryParameter(QueryKey.CODE)
            ?: throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.MISSING_CODE_URI_PARAMETER,
            )
    }
}
