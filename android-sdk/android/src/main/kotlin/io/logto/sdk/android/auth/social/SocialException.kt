package io.logto.sdk.android.auth.social

import java.lang.RuntimeException

class SocialException(
    type: Type,
) : RuntimeException(type.code, null) {
    val code = type.code
    var socialCode: String? = null
    var socialMessage: String? = null

    enum class Type(val code: String) {
        INVALID_JSON("invalid_json"),
        INVALID_REDIRECT_TO("invalid_redirect_to"),
        INVALID_CALLBACK_URI("invalid_callback_uri"),
        UNABLE_TO_CONSTRUCT_CALLBACK_URI("unable_to_construct_callback_uri"),
        UNKNOWN_SOCIAL_SCHEME("unknown_social_scheme"),
        INSUFFICIENT_INFORMATION("insufficient_information"),
        AUTHENTICATION_FAILED("authentication_failed"),
    }
}
