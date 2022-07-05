package io.logto.sdk.android.type

import io.logto.sdk.core.constant.PromptValue
import io.logto.sdk.core.util.ScopeUtils

class LogtoConfig(
    val endpoint: String,
    val appId: String,
    scopes: List<String>? = null,
    val resources: List<String>? = null,
    val usingPersistStorage: Boolean = true,
    val prompt: String = PromptValue.CONSENT,
) {
    val scopes = ScopeUtils.withReservedScopes(scopes)
}
