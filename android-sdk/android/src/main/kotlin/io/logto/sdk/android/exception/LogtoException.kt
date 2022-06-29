package io.logto.sdk.android.exception

class LogtoException(
    type: Type,
    cause: Throwable? = null,
) : RuntimeException(type.name, cause) {
    var detail: String? = null
    enum class Type {
        NOT_AUTHENTICATED,
        UNGRANTED_RESOURCE_FOUND,
        USER_CANCELED,
        INVALID_REDIRECT_URI,
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
        ALIPAY_APP_ID_NO_FOUND,
        ALIPAY_AUTH_FAILED,
        WECHAT_APP_ID_NO_FOUND,
        WECHAT_AUTH_FAILED,
    }
}
