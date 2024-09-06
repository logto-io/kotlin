package io.logto.sdk.android.type

import io.logto.sdk.core.type.DirectSignInOptions

class SignInOptions(
    val redirectUri: String,
    val prompt: String? = null,
    val firstScreen: String? = null,
    val identifiers: List<String>? = null,
    val directSignIn: DirectSignInOptions? = null,
    val loginHint: String? = null,
    val extraParams: Map<String, String>? = null,
)
