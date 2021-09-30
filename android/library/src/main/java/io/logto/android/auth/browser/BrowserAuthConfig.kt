package io.logto.android.auth.browser

import io.logto.android.callback.AuthenticationCallback
import io.logto.android.config.LogtoConfig

data class BrowserAuthConfig(
    val codeVerifier: String,
    val authUrl: String,
    val logtoConfig: LogtoConfig,
    val authenticationCallback: AuthenticationCallback,
)
