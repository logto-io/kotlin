package io.logto.sdk.android.callback

interface RetrieveCallback<T> {
    fun onResult(throwable: Throwable?, result: T?)
}
