package io.logto.sdk.android.completion

import java.lang.RuntimeException

fun interface Completion<T : RuntimeException, U : Any> {
    fun onComplete(exception: T?, result: U?)
}

fun interface EmptyCompletion<T : RuntimeException> {
    fun onComplete(exception: T?)
}
