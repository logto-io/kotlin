package io.logto.sdk.core.http

interface HttpCompletion<T> {
    fun onComplete(throwable: Throwable?, result: T?)
}

interface HttpEmptyCompletion {
    fun onComplete(throwable: Throwable?)
}
