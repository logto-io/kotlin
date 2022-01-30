package io.logto.sdk.core.exception

class ResponseException(
    message: Enum<Message>,
    cause: Throwable? = null,
) : RuntimeException(message.name, cause) {
    var description: String? = null

    enum class Message {
        ERROR_RESPONSE,
        EMPTY_RESPONSE,
    }
}
