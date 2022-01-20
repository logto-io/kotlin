package io.logto.sdk.core.util

import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.exception.LogtoException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object UriUtils {
    fun verifyAndParseCodeFromCallbackUri(
        callbackUri: String,
        redirectUri: String,
        state: String,
    ): String {
        val parsedUri = callbackUri.toHttpUrlOrNull()
            ?: throw LogtoException.CallbackUriVerificationException(
                LogtoException.CallbackUriVerification.INVALID_URI_FORMAT
            )

        if (!callbackUri.startsWith(redirectUri)) {
            throw LogtoException.CallbackUriVerificationException(
                LogtoException.CallbackUriVerification.URI_MISMATCHED
            )
        }

        parsedUri.queryParameter(QueryKey.ERROR)?.let {
            throw LogtoException.CallbackUriVerificationException(
                LogtoException.CallbackUriVerification.ERROR_FOUND_IN_URI
            ).apply {
                error = it
                errorDesc = parsedUri.queryParameter(QueryKey.ERROR_DESCRIPTION)
            }
        }

        parsedUri.queryParameter(QueryKey.STATE)?.let {
            if (it != state) {
                throw LogtoException.CallbackUriVerificationException(
                    LogtoException.CallbackUriVerification.STATE_MISMATCHED
                )
            }
        } ?: throw LogtoException.CallbackUriVerificationException(
            LogtoException.CallbackUriVerification.MISSING_STATE_URI_PARAMETER
        )

        return parsedUri.queryParameter(QueryKey.CODE)
            ?: throw LogtoException.CallbackUriVerificationException(
                LogtoException.CallbackUriVerification.MISSING_CODE_URI_PARAMETER
            )
    }
}
