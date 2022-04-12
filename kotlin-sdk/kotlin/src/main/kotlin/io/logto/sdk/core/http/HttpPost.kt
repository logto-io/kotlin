package io.logto.sdk.core.http

import com.google.gson.JsonSyntaxException
import io.logto.sdk.core.exception.ResponseException
import okhttp3.RequestBody

inline fun <reified T : Any> httpPost(
    uri: String,
    body: RequestBody,
    headers: Map<String, String>?,
    completion: HttpCompletion<T>,
) {
    httpPost(uri, body, headers) httpRawPost@{ throwable, response ->
        throwable?.let {
            completion.onComplete(throwable, null)
            return@httpRawPost
        }
        try {
            completion.onComplete(null, gson.fromJson(response, T::class.java))
        } catch (jsonSyntaxException: JsonSyntaxException) {
            completion.onComplete(
                ResponseException(ResponseException.Message.ERROR_RESPONSE, jsonSyntaxException),
                null
            )
        }
    }
}

inline fun <reified T : Any> httpPost(uri: String, body: RequestBody, completion: HttpCompletion<T>) =
    httpPost(uri, body, null, completion)

fun httpPost(
    uri: String,
    body: RequestBody,
    completion: HttpEmptyCompletion,
) = makeRequest(uri, body, null, completion)

@JvmName("httpRawPost")
fun httpPost(
    uri: String,
    body: RequestBody,
    headers: Map<String, String>?,
    completion: HttpRawStringCompletion,
) = makeRequest(uri, body, headers, completion)

@JvmName("httpRawPost")
fun httpPost(
    uri: String,
    body: RequestBody,
    completion: HttpRawStringCompletion,
) = makeRequest(uri, body, null, completion)
