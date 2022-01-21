package io.logto.sdk.core.extension

import io.logto.sdk.core.constant.ReservedScope

fun List<String>?.ensureDefaultScopes(): List<String> = this?.let {
    if (it.contains(ReservedScope.OPENID) and it.contains(ReservedScope.OFFLINE_ACCESS)) {
        return it
    }

    val mutableScopes = it.toMutableList()

    if (!mutableScopes.contains(ReservedScope.OPENID)) {
        mutableScopes.add(ReservedScope.OPENID)
    }

    if (!mutableScopes.contains(ReservedScope.OFFLINE_ACCESS)) {
        mutableScopes.add(ReservedScope.OFFLINE_ACCESS)
    }

    return mutableScopes.toList()
} ?: listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS)
