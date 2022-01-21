package io.logto.sdk.core.extension

import io.logto.sdk.core.constant.ReservedScope

fun List<String>?.ensureDefaultScopes(): List<String> =
    ((this ?: listOf()) + listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS)).distinct()
