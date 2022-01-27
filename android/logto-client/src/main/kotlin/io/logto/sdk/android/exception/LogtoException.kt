package io.logto.sdk.android.exception

class LogtoException(
    message: Enum<Message>,
    cause: Throwable? = null,
) : RuntimeException(message.name, cause) {
    var detail: String? = null
    enum class Message {
        NOT_AUTHENTICATED,
        MISSING_REFRESH_TOKEN,
        RESOURCE_IS_NOT_GRANTED,
        SCOPES_ARE_NOT_ALL_GRANTED,
    }
}
