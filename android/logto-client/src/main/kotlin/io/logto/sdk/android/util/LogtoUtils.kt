package io.logto.sdk.android.util

import kotlin.math.floor

object LogtoUtils {
    private const val MILLIS_PER_SECOND = 1000L

    fun nowRoundToSec() = floor((System.currentTimeMillis() / MILLIS_PER_SECOND).toDouble()).toLong()

    fun expiresAtFrom(startTime: Long, lifetime: Long): Long {
        return startTime + lifetime
    }
}
