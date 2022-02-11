package io.logto.sdk.core.exception

class ResponseException(
    message: Enum<Message>,
    cause: Throwable? = null,
) : RuntimeException(message.name, cause) {
    var responseMessage: String? = null
    var responseContent: String? = null

    enum class Message {
        REQUEST_FAILED,
        ERROR_RESPONSE,
        EMPTY_RESPONSE,
    }
}
