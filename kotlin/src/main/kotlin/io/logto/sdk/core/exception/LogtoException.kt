package io.logto.sdk.core.exception

open class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class VerificationException(
        message: Enum<Verification>,
        cause: Throwable? = null,
    ) : LogtoException(message.name, cause)

    enum class Verification {
        INVALID_URI_FORMAT,
        URI_MISMATCHED,
        MISSING_CODE_URI_PARAMETER,
        MISSING_STATE_URI_PARAMETER,
        STATE_MISMATCHED,
    }

    class OidcProviderException(message: String, cause: Throwable? = null) : LogtoException(message, cause)
}
