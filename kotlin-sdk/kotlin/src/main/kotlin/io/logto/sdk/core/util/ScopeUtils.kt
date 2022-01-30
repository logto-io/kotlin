package io.logto.sdk.core.util

import io.logto.sdk.core.constant.ReservedScope

object ScopeUtils {
    fun withDefaultScopes(scope: List<String>?): List<String> =
        ((scope ?: listOf()) + listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS)).distinct()
}
