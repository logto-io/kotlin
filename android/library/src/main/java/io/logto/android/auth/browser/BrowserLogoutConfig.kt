package io.logto.android.auth.browser

import io.logto.android.config.LogtoConfig

data class BrowserLogoutConfig(
    val logtoConfig: LogtoConfig,
    val idToken: String,
)
