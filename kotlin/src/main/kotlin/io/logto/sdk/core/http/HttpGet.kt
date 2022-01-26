package io.logto.sdk.core.http

inline fun <reified T> httpGet(uri: String, completion: HttpCompletion<T>) = httpGet(uri, null, completion)

inline fun <reified T> httpGet(
    uri: String,
    headers: Map<String, String>? = null,
    completion: HttpCompletion<T>,
) = makeRequest(uri, null, headers, completion)

fun httpGet(uri: String, completion: HttpEmptyCompletion) = makeRequest(uri, null, null, completion)
