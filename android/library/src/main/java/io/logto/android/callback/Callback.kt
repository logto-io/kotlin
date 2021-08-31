package io.logto.android.callback

interface Callback<T> {
    fun onSuccess(result: T)
    fun onFailed(error: Error)
}
