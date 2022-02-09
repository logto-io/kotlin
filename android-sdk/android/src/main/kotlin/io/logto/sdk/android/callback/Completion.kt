package io.logto.sdk.android.callback

fun interface Completion<T : Any> {
    fun onComplete(throwable: Throwable?, result: T?)
}

fun interface EmptyCompletion {
    fun onComplete(throwable: Throwable?)
}
