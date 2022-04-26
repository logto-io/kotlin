package io.logto.sdk.android.type

import io.logto.sdk.core.util.ScopeUtils

class LogtoConfig(
    val endpoint: String,
    val appId: String,
    scopes: List<String>? = null,
    val resources: List<String>? = null,
    val usingPersistStorage: Boolean = false,
) {
    val scopes = ScopeUtils.withReservedScopes(scopes)
}
