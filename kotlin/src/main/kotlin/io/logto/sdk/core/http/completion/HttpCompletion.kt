package io.logto.sdk.core.http.completion

interface HttpCompletion<T> {
    fun onComplete(throwable: Throwable?, result: T?)
}
