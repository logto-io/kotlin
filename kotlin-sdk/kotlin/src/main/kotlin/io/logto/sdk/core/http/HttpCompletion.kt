package io.logto.sdk.core.http

fun interface HttpCompletion<T : Any> {
    fun onComplete(throwable: Throwable?, response: T?)
}

fun interface HttpEmptyCompletion {
    fun onComplete(throwable: Throwable?)
}
