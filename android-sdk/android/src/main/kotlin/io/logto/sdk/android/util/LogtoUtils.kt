package io.logto.sdk.android.util

import android.net.Uri
import java.util.Calendar
import kotlin.math.floor

object LogtoUtils {
    private const val MILLIS_PER_SECOND = 1000L

    fun nowRoundToSec() = floor((Calendar.getInstance().timeInMillis / MILLIS_PER_SECOND).toDouble()).toLong()

    fun isValidRedirectUri(uri: String): Boolean {
        val parsedUri = Uri.parse(uri)
        return parsedUri.scheme != null && parsedUri.isHierarchical && parsedUri.fragment == null
    }

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
