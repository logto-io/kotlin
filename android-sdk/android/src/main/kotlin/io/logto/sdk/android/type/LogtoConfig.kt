package io.logto.sdk.android.type

import io.logto.sdk.core.constant.PromptValue
import io.logto.sdk.core.constant.ReservedResource
import io.logto.sdk.core.constant.UserScope
import io.logto.sdk.core.util.ScopeUtils

class LogtoConfig(
    val endpoint: String,
    val appId: String,
    scopes: List<String>? = null,
    resources: List<String>? = null,
    val usingPersistStorage: Boolean = true,
    val prompt: String = PromptValue.CONSENT,
) {
    /**
     * Normalize the Logto client configuration per the following rules:
     *
     * - Add default scopes (`openid`, `offline_access` and `profile`) if not provided.
     * - Add `ReservedResource.Organization` to resources if `UserScope.Organizations` is included in scopes.
     */
    val scopes = ScopeUtils.withDefaultScopes(scopes)
    val resources = if (this.scopes.contains(UserScope.ORGANIZATIONS)) {
        (resources.orEmpty() + ReservedResource.ORGANIZATION)
    } else {
        resources
    }
}
