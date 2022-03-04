package io.logto.sdk.android.completion

import io.logto.sdk.android.exception.LogtoException

fun interface Completion<T : Any> {
    fun onComplete(logtoException: LogtoException?, result: T?)
}

fun interface EmptyCompletion {
    fun onComplete(logtoException: LogtoException?)
}
