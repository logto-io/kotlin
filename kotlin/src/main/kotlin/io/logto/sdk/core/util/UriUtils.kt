package io.logto.sdk.core.util

import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.exception.CallbackUriVerificationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object UriUtils {
    fun verifyAndParseCodeFromCallbackUri(
        callbackUri: String,
        redirectUri: String,
        state: String,
    ): String {
        val parsedUri = callbackUri.toHttpUrlOrNull()
            ?: throw CallbackUriVerificationException(
                CallbackUriVerificationException.Message.INVALID_URI_FORMAT
            )

        if (!callbackUri.startsWith(redirectUri)) {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Message.URI_MISMATCHED
            )
        }

        parsedUri.queryParameter(QueryKey.ERROR)?.let {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Message.ERROR_FOUND_IN_URI
            ).apply {
                error = it
                errorDesc = parsedUri.queryParameter(QueryKey.ERROR_DESCRIPTION)
            }
        }

        parsedUri.queryParameter(QueryKey.STATE)?.let {
            if (it != state) {
                throw CallbackUriVerificationException(
                    CallbackUriVerificationException.Message.STATE_MISMATCHED
                )
            }
        } ?: throw CallbackUriVerificationException(
            CallbackUriVerificationException.Message.MISSING_STATE_URI_PARAMETER
        )

        return parsedUri.queryParameter(QueryKey.CODE)
            ?: throw CallbackUriVerificationException(
                CallbackUriVerificationException.Message.MISSING_CODE_URI_PARAMETER
            )
    }
}
