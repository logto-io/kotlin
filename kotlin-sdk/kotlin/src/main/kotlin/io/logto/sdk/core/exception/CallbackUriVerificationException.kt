package io.logto.sdk.core.exception

class CallbackUriVerificationException(
    type: Type,
    cause: Throwable? = null,
) : RuntimeException(type.name, cause) {
    var error: String? = null
    var errorDesc: String? = null

    enum class Type {
        INVALID_URI_FORMAT,
        URI_MISMATCHED,
        ERROR_FOUND_IN_URI,
        MISSING_CODE_URI_PARAMETER,
        MISSING_STATE_URI_PARAMETER,
        STATE_MISMATCHED,
    }
}
