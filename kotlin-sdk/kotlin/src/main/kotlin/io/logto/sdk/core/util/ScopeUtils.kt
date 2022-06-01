package io.logto.sdk.core.util

import io.logto.sdk.core.constant.ReservedScope

object ScopeUtils {
    /**
     * Ensure the scope list contains `open_id` and `offline_access`
     * @param[scopes] The origin scope list
     * @return The scope list which contains `open_id` and `offline_access`
     */
    fun withReservedScopes(scopes: List<String>?): List<String> = (
        (scopes ?: listOf()) + listOf(
            ReservedScope.OPENID,
            ReservedScope.OFFLINE_ACCESS,
            ReservedScope.PROFILE,
        )
        ).distinct()
}
