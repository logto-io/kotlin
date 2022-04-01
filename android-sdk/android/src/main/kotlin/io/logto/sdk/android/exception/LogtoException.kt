package io.logto.sdk.android.exception

class LogtoException(
    message: Enum<Message>,
    cause: Throwable? = null,
) : RuntimeException(message.name, cause) {
    var detail: String? = null
    enum class Message {
        NOT_AUTHENTICATED,
        NO_REFRESH_TOKEN_FOUND,
        UNGRANTED_RESOURCE_FOUND,
        USER_CANCELED,
        INVALID_CALLBACK_URI,
        UNABLE_TO_FETCH_OIDC_CONFIG,
        UNABLE_TO_FETCH_TOKEN_BY_AUTHORIZATION_CODE,
        UNABLE_TO_FETCH_TOKEN_BY_REFRESH_TOKEN,
        UNABLE_TO_REVOKE_TOKEN,
        UNABLE_TO_PARSE_ID_TOKEN_CLAIMS,
        UNABLE_TO_FETCH_USER_INFO,
        UNABLE_TO_FETCH_JWKS_JSON,
        UNABLE_TO_PARSE_JWKS,
        INVALID_ID_TOKEN,
    }
}
