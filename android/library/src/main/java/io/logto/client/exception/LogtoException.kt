package io.logto.client.exception

class LogtoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    companion object {
        const val UNKNOWN_ERROR = "Unknown Error"
        const val NOT_AUTHENTICATED = "Not authenticated"
        const val REFRESH_TOKEN_IS_NOT_SUPPORTED = "Refresh token is not supported"
        const val SIGN_IN_FAILED = "Sign in failed"
        const val INVALID_REDIRECT_URI = "Invalid redirect uri"
        const val EMPTY_REDIRECT_URI = "Empty redirect uri"
        const val MISSING_AUTHORIZATION_CODE = "Missing authorization code"
        const val SIGN_OUT_FAILED = "Sign out failed"
        const val INVALID_JWT = "Invalid jwt"
        const val REQUEST_OIDC_CONFIGURATION_FAILED = "Request oidc configuration failed"
        const val REQUEST_TOKEN_FAILED = "Request token failed"
        const val REQUEST_JWKS_FAILED = "Request jwks failed"
        const val ENCRYPT_ALGORITHM_NOT_SUPPORTED = "Encrypt Algorithm Not Supported"
        const val CODE_CHALLENGE_ENCODED_FAILED = "Code challenge encoded failed"
    }
}
