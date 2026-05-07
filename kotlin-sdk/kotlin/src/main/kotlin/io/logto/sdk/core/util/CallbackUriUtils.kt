package io.logto.sdk.core.util

import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.exception.CallbackUriVerificationException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object CallbackUriUtils {
    /**
     * Verify and parse code from callback URI
     * @param[callbackUri] The callback URI to be verified
     * @param[redirectUri] The redirect URI on sign in
     * @param[state] The state on sign in
     * @return Authorization code
     * @throws[CallbackUriVerificationException]
     */
    fun verifyAndParseCodeFromCallbackUri(
        callbackUri: String,
        redirectUri: String,
        state: String,
    ): String {
        val parsedCallbackUri = parseUri(callbackUri)
        val parsedRedirectUri = parseUri(redirectUri)

        if (!parsedCallbackUri.matchesRedirectUri(parsedRedirectUri)) {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.URI_MISMATCHED,
            )
        }

        parsedCallbackUri.queryParameters[QueryKey.ERROR]?.let {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.ERROR_FOUND_IN_URI,
            ).apply {
                error = it
                errorDesc = parsedCallbackUri.queryParameters[QueryKey.ERROR_DESCRIPTION]
            }
        }

        parsedCallbackUri.queryParameters[QueryKey.STATE]?.let {
            if (it != state) {
                throw CallbackUriVerificationException(
                    CallbackUriVerificationException.Type.STATE_MISMATCHED,
                )
            }
        } ?: throw CallbackUriVerificationException(
            CallbackUriVerificationException.Type.MISSING_STATE_URI_PARAMETER,
        )

        return parsedCallbackUri.queryParameters[QueryKey.CODE]
            ?: throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.MISSING_CODE_URI_PARAMETER,
            )
    }

    private fun parseUri(uri: String): ParsedUri {
        val parsedUri = try {
            URI(uri)
        } catch (cause: URISyntaxException) {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.INVALID_URI_FORMAT,
                cause,
            )
        }

        if (parsedUri.scheme == null || parsedUri.isOpaque || parsedUri.rawFragment != null) {
            throw CallbackUriVerificationException(
                CallbackUriVerificationException.Type.INVALID_URI_FORMAT,
            )
        }

        return ParsedUri(
            scheme = parsedUri.scheme,
            authority = parsedUri.rawAuthority,
            path = parsedUri.rawPath.orEmpty(),
            queryParameters = parseQueryParameters(parsedUri.rawQuery),
        )
    }

    private fun parseQueryParameters(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrEmpty()) {
            return emptyMap()
        }

        return rawQuery.split("&").fold(mutableMapOf()) { queryParameters, parameter ->
            val separatorIndex = parameter.indexOf("=")
            val rawName = if (separatorIndex == -1) parameter else parameter.substring(0, separatorIndex)
            val rawValue = if (separatorIndex == -1) "" else parameter.substring(separatorIndex + 1)

            queryParameters.putIfAbsent(decodeQueryComponent(rawName), decodeQueryComponent(rawValue))
            queryParameters
        }
    }

    private fun decodeQueryComponent(value: String): String = try {
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    } catch (cause: IllegalArgumentException) {
        throw CallbackUriVerificationException(
            CallbackUriVerificationException.Type.INVALID_URI_FORMAT,
            cause,
        )
    }

    private data class ParsedUri(
        val scheme: String,
        val authority: String?,
        val path: String,
        val queryParameters: Map<String, String>,
    ) {
        fun matchesRedirectUri(redirectUri: ParsedUri): Boolean =
            scheme == redirectUri.scheme &&
                authority == redirectUri.authority &&
                path == redirectUri.path &&
                redirectUri.queryParameters.all { (key, value) -> queryParameters[key] == value }
    }
}
