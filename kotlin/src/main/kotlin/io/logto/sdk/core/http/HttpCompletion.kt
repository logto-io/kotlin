package io.logto.sdk.core.http

interface HttpCompletion<T> {
    fun onComplete(throwable: Throwable?, response: T?)
}

interface HttpEmptyCompletion {
    fun onComplete(throwable: Throwable?)
}
