package io.logto.sdk.core.exception

class UriConstructionException(
    message: Enum<Message>,
    cause: Throwable? = null,
) : RuntimeException(message.name, cause) {
    enum class Message {
        INVALID_ENDPOINT,
    }
}
