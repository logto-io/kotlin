package io.logto.android.auth.browser

import io.logto.android.config.LogtoConfig
import io.logto.android.model.Credential

data class BrowserLoginConfig(
    val codeVerifier: String,
    val authUrl: String,
    val logtoConfig: LogtoConfig,
    val onComplete: (error: Error?, credential: Credential?) -> Unit
)
