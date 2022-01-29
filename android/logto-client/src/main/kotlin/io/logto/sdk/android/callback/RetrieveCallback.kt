package io.logto.sdk.android.callback

fun interface RetrieveCallback<T> {
    fun onResult(throwable: Throwable?, result: T?)
}
