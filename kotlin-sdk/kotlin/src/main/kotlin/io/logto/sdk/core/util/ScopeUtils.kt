package io.logto.sdk.core.util

import io.logto.sdk.core.constant.ReservedScope
import io.logto.sdk.core.constant.UserScope

object ScopeUtils {
    /**
     * Ensure the scope list contains `open_id`, `offline_access` and `profile`
     * @param[scopes] The origin scope list
     * @return The scope list which contains `open_id`, `offline_access` and `profile`
     */
    fun withDefaultScopes(scopes: List<String>?): List<String> = (
        (scopes ?: listOf()) + listOf(
            ReservedScope.OPENID,
            ReservedScope.OFFLINE_ACCESS,
            UserScope.PROFILE,
        )
        ).distinct()
}
