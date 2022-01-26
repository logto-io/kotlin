package io.logto.sdk.core.http

import io.logto.sdk.core.http.completion.HttpCompletion
import io.logto.sdk.core.http.completion.HttpEmptyCompletion
import okhttp3.RequestBody

inline fun <reified T> httpPost(
    uri: String,
    body: RequestBody,
    completion: HttpCompletion<T>,
) = httpPost(uri, body, null, completion)

inline fun <reified T> httpPost(
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
