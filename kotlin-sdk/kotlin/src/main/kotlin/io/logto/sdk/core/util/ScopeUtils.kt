package io.logto.sdk.core.util

import io.logto.sdk.core.constant.ReservedScope

object ScopeUtils {
    fun withReservedScopes(scopes: List<String>?): List<String> =
        ((scopes ?: listOf()) + listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS)).distinct()
}
