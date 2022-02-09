package io.logto.sdk.core.http

import okhttp3.RequestBody

inline fun <reified T : Any> httpPost(
    uri: String,
    body: RequestBody,
    completion: HttpCompletion<T>,
) = httpPost(uri, body, null, completion)

inline fun <reified T : Any> httpPost(
    uri: String,
    body: RequestBody,
    headers: Map<String, String>? = null,
    completion: HttpCompletion<T>
) = makeRequest(uri, body, headers, completion)

fun httpPost(
    uri: String,
    body: RequestBody,
    completion: HttpEmptyCompletion,
) = makeRequest(uri, body, null, completion)
