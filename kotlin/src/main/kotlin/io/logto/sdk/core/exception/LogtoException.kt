package io.logto.sdk.core.exception

open class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class CallbackUriVerificationException(
        message: Enum<CallbackUriVerification>,
        cause: Throwable? = null,
    ) : LogtoException(message.name, cause) {
        var error: String? = null
        var errorDesc: String? = null
    }

    enum class CallbackUriVerification {
        INVALID_URI_FORMAT,
        URI_MISMATCHED,
        ERROR_FOUND_IN_URI,
        MISSING_CODE_URI_PARAMETER,
        MISSING_STATE_URI_PARAMETER,
        STATE_MISMATCHED,
    }
}
