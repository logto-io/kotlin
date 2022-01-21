package io.logto.sdk.core.extension

import io.logto.sdk.core.constant.ReservedScope

fun List<String>?.ensureDefaultScopes(): List<String> = this?.let {
    if (it.contains(ReservedScope.OPENID) and it.contains(ReservedScope.OFFLINE_ACCESS)) {
        return it
    }

    val mutableList = it.toMutableList()

    if (!mutableList.contains(ReservedScope.OPENID)) {
        mutableList.add(ReservedScope.OPENID)
    }

    if (!mutableList.contains(ReservedScope.OFFLINE_ACCESS)) {
        mutableList.add(ReservedScope.OFFLINE_ACCESS)
    }

    return mutableList.toList()
} ?: listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS)
