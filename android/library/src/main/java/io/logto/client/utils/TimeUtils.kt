package io.logto.client.utils

import kotlin.math.floor

object TimeUtils {
    private const val MILLIS_PER_SECOND = 1000L

    fun expiresAtFrom(startTime: Long, lifetime: Long): Long {
        return startTime + lifetime
    }

    fun expiresAtFromNow(lifetime: Long): Long {
        return expiresAtFrom(nowRoundToSec(), lifetime)
    }

    fun nowRoundToSec() = floor((System.currentTimeMillis() / MILLIS_PER_SECOND).toDouble()).toLong()
}
