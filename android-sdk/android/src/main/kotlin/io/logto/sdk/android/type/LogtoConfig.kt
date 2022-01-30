package io.logto.sdk.android.type

import io.logto.sdk.core.util.ScopeUtils

class LogtoConfig(
    val endpoint: String,
    val clientId: String,
    scope: List<String>? = null,
    val resource: List<String>? = null,
    val usingPersistStorage: Boolean = false,
) {
    val scope = ScopeUtils.withDefaultScopes(scope)
}