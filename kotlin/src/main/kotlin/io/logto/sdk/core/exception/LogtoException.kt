package io.logto.sdk.core.exception

open class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class CallbackUriVerificationException(
        message: Enum<CallbackUriVerification>,
        cause: Throwable? = null,
    ) : LogtoException(message.name, cause)

    enum class CallbackUriVerification {
        INVALID_URI_FORMAT,
        URI_MISMATCHED,
        MISSING_CODE_URI_PARAMETER,
        MISSING_STATE_URI_PARAMETER,
        STATE_MISMATCHED,
    }

    class RedirectUriReturnedException(
        val error: String,
        val errorDesc: String?,
    ) : LogtoException("Redirect Uri Returned Exception")
}
