package io.logto.sdk.android.auth.logto

import android.annotation.SuppressLint
import android.net.Uri

private const val PERCENT_ENCODED_CHARACTER_LENGTH = 3
private const val HEX_RADIX = 16

internal object LogtoAuthManager {
    @SuppressLint("StaticFieldLeak")
    internal var logtoAuthSession: LogtoAuthSession? = null

    fun handleAuthStart(authSession: LogtoAuthSession) {
        logtoAuthSession = authSession
    }

    fun handleCallbackUri(uri: Uri) {
        logtoAuthSession?.handleCallbackUri(uri)
        logtoAuthSession = null
    }

    fun handleUserCancel() {
        logtoAuthSession?.handleUserCancel()
        logtoAuthSession = null
    }

    fun isLogtoAuthResult(uri: Uri) = logtoAuthSession?.let { authSession ->
        uri.matchesRedirectUri(Uri.parse(authSession.signInOptions.redirectUri))
    } ?: false

    private fun Uri.matchesRedirectUri(redirectUri: Uri): Boolean {
        if (!isHierarchical || !redirectUri.isHierarchical) {
            return false
        }
        if (fragment != null || redirectUri.fragment != null) {
            return false
        }

        return scheme?.lowercase() == redirectUri.scheme?.lowercase() &&
            normalizedAuthority() == redirectUri.normalizedAuthority() &&
            encodedPath.orEmpty().normalizePath() == redirectUri.encodedPath.orEmpty().normalizePath() &&
            redirectUri.queryParameterNames.all { queryKey ->
                getQueryParameters(queryKey) == redirectUri.getQueryParameters(queryKey)
            }
    }

    private fun Uri.normalizedAuthority(): String? {
        if (encodedAuthority == null) {
            return null
        }

        host?.let { host ->
            val userInfo = encodedUserInfo?.let { "$it@" }.orEmpty()
            val port = if (port == -1) "" else ":$port"
            return "$userInfo${host.lowercase()}$port"
        }

        return encodedAuthority
    }

    private fun String.normalizePath(): String = normalizePercentEncodedUnreserved()

    private fun String.normalizePercentEncodedUnreserved(): String {
        val normalizedValue = StringBuilder()
        var index = 0

        while (index < length) {
            val normalizedCharacter = normalizedPercentEncodedCharacterAt(index)
            if (normalizedCharacter != null) {
                normalizedValue.append(normalizedCharacter)
                index += PERCENT_ENCODED_CHARACTER_LENGTH
                continue
            }

            normalizedValue.append(this[index])
            index += 1
        }

        return normalizedValue.toString()
    }

    private fun String.normalizedPercentEncodedCharacterAt(index: Int): String? {
        if (this[index] != '%' || index + PERCENT_ENCODED_CHARACTER_LENGTH > length) {
            return null
        }

        val hex = substring(index + 1, index + PERCENT_ENCODED_CHARACTER_LENGTH)
        val char = hex.toIntOrNull(HEX_RADIX)?.toChar() ?: return null
        return if (char.isUnreservedUriCharacter()) {
            char.toString()
        } else {
            "%${hex.uppercase()}"
        }
    }

    private fun Char.isUnreservedUriCharacter(): Boolean = when (this) {
        in 'A'..'Z',
        in 'a'..'z',
        in '0'..'9',
        '-',
        '.',
        '_',
        '~',
        -> true
        else -> false
    }
}
