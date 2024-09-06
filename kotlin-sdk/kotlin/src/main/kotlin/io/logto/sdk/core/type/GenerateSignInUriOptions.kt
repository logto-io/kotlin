package io.logto.sdk.core.type

class GenerateSignInUriOptions(
    val authorizationEndpoint: String,
    val clientId: String,
    val redirectUri: String,
    val codeChallenge: String,
    val state: String,
    val scopes: List<String>? = null,
    val resources: List<String>? = null,
    val prompt: String? = null,
)
