package io.logto.sdk.android.util

import java.util.Calendar
import kotlin.math.floor

object LogtoUtils {
    private const val MILLIS_PER_SECOND = 1000L

    fun nowRoundToSec() = floor((Calendar.getInstance().timeInMillis / MILLIS_PER_SECOND).toDouble()).toLong()

    fun expiresAtFrom(startTime: Long, lifetime: Long): Long {
        return startTime + lifetime
    }

    fun isDependencyInstalled(identifyClassName: String) = try {
        Class.forName(identifyClassName)
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
