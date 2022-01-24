package io.logto.sdk.core.callback

interface HttpCompletion<T> {
    fun onComplete(throwable: Throwable?, result: T)
}
