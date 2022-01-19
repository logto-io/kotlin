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
        if (!callbackUri.startsWith(redirectUri)) {
            throw LogtoException.VerificationException(LogtoException.Verification.URI_MISMATCHED)
        }

        val parsedUri = callbackUri.toHttpUrlOrNull()
            ?: throw LogtoException.VerificationException(LogtoException.Verification.INVALID_URI_FORMAT)

        parsedUri.queryParameter(QueryKey.ERROR)?.let {
            throw LogtoException.RedirectUriReturnedException(
                error = it,
                errorDesc = parsedUri.queryParameter(QueryKey.ERROR_DESCRIPTION)
            )
        }

        parsedUri.queryParameter(QueryKey.STATE)?.let {
            if (it != state) {
                throw LogtoException.VerificationException(LogtoException.Verification.STATE_MISMATCHED)
            }
        } ?: throw LogtoException.VerificationException(LogtoException.Verification.MISSING_STATE_URI_PARAMETER)

        return parsedUri.queryParameter(QueryKey.CODE)
            ?: throw LogtoException.VerificationException(LogtoException.Verification.MISSING_CODE_URI_PARAMETER)
    }
}
