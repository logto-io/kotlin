package io.logto.sdk.android.type

import io.logto.sdk.core.extension.ensureDefaultScopes

class LogtoConfig(
    val endpoint: String,
    val clientId: String,
    scope: List<String>? = null,
    val resource: List<String>? = null,
    val usingPersistStorage: Boolean = false,
) {
    val scope by lazy { scope.ensureDefaultScopes() }
}
