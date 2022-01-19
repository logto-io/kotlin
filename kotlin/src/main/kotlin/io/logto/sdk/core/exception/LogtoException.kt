package io.logto.sdk.core.exception

open class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class DecodingException(message: Enum<Decoding>, cause: Throwable? = null) : LogtoException(message.name, cause)
    enum class Decoding {
        INVALID_JWT
    }
}
