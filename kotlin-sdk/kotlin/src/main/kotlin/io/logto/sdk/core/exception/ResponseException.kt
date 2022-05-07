package io.logto.sdk.core.exception

class ResponseException(
    type: Type,
    cause: Throwable? = null,
) : RuntimeException(type.name, cause) {
    var responseMessage: String? = null
    var responseContent: String? = null

    enum class Type {
        REQUEST_FAILED,
        ERROR_RESPONSE,
        EMPTY_RESPONSE,
    }
}
