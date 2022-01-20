package io.logto.sdk.core.exception

class CallbackUriVerificationException(
    message: Enum<Message>,
    cause: Throwable? = null,
) : RuntimeException(message.name, cause) {
    var error: String? = null
    var errorDesc: String? = null

    enum class Message {
        INVALID_URI_FORMAT,
        URI_MISMATCHED,
        ERROR_FOUND_IN_URI,
        MISSING_CODE_URI_PARAMETER,
        MISSING_STATE_URI_PARAMETER,
        STATE_MISMATCHED,
    }
}
