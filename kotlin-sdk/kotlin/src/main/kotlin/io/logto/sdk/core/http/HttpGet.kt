package io.logto.sdk.core.http

import com.google.gson.JsonSyntaxException
import io.logto.sdk.core.exception.ResponseException

inline fun <reified T : Any> httpGet(
    uri: String,
    headers: Map<String, String>?,
    completion: HttpCompletion<T>,
) {
    httpGet(uri, headers) httpRawGet@{ throwable, response ->
        throwable?.let {
            completion.onComplete(throwable, null)
            return@httpRawGet
        }
        try {
            completion.onComplete(null, gson.fromJson(response, T::class.java))
        } catch (jsonSyntaxException: JsonSyntaxException) {
            completion.onComplete(
                ResponseException(ResponseException.Type.ERROR_RESPONSE, jsonSyntaxException),
                null,
            )
        }
    }
}

inline fun <reified T : Any> httpGet(uri: String, completion: HttpCompletion<T>) =
    httpGet(uri, null, completion)

fun httpGet(uri: String, completion: HttpEmptyCompletion) = makeRequest(uri, null, null, completion)

@JvmName("httpRawGet")
fun httpGet(
    uri: String,
    headers: Map<String, String>?,
    completion: HttpRawStringCompletion,
) = makeRequest(uri, null, headers, completion)

@JvmName("httpRawGet")
fun httpGet(uri: String, completion: HttpRawStringCompletion) = httpGet(uri, null, completion)
