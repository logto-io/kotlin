package io.logto.sdk.android.callback

fun interface Completion<T> {
    fun onComplete(throwable: Throwable?, result: T?)
}
