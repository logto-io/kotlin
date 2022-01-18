package io.logto.sdk.core.exception

open class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class System(message: Enum<SystemException>, cause: Throwable?): LogtoException(message.name, cause)
    enum class SystemException {
        ENCODED_ALGORITHM_NOT_SUPPORTED,
        ENCODING_NOT_SUPPORTED
    }
}
