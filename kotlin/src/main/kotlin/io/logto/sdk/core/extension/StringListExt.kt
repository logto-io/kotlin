package io.logto.sdk.core.extension

import io.logto.sdk.core.constant.ReservedScope

fun List<String>?.ensureDefaultScopes(): List<String> = this?.let {
    if (it.contains(ReservedScope.OPEN_ID) and it.contains(ReservedScope.OFFLINE_ACCESS)) {
        return it
    }

    val mutableList = it.toMutableList()

    if (!mutableList.contains(ReservedScope.OPEN_ID)) {
        mutableList.add(ReservedScope.OPEN_ID)
    }

    if (!mutableList.contains(ReservedScope.OFFLINE_ACCESS)) {
        mutableList.add(ReservedScope.OFFLINE_ACCESS)
    }

    mutableList.toList()
} ?: listOf(ReservedScope.OPEN_ID, ReservedScope.OFFLINE_ACCESS)
