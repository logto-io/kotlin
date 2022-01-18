package io.logto.sdk.core.exception

class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    object System {
        const val ENCODED_ALGORITHM_NOT_SUPPORTED = "Encoded Algorithm Not Supported"
        const val ENCODING_NOT_SUPPORTED = "Encoding Not Supported"
    }
}
