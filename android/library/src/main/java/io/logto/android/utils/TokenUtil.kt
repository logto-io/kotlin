package io.logto.android.utils

import io.logto.android.model.Credential

object TokenUtil {
    private const val MILLISECONDS = 1000L

    fun expiresAt(credential: Credential): Long {
        return System.currentTimeMillis() + credential.expiresIn * MILLISECONDS
    }
}
