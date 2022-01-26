package io.logto.sdk.core.http

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import io.logto.sdk.core.exception.ResponseException
import io.logto.sdk.core.http.completion.HttpCompletion
import io.logto.sdk.core.http.completion.HttpEmptyCompletion
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

val gson: Gson by lazy {
    GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
}

val httpClient by lazy { OkHttpClient() }

inline fun <reified T> makeRequest(
    uri: String,
    body: RequestBody? = null,
    headers: Map<String, String>? = null,
    completion: HttpCompletion<T>,
) = makeRequest(
    uri,
    body,
    headers,
    object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            completion.onComplete(e, null)
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                completion.onComplete(
                    ResponseException(ResponseException.Message.ERROR_RESPONSE).apply {
                        description = response.message
                    },
                    null
                )
                return
            }

            try {
                completion.onComplete(
                    null,
                    response.let { it.body?.string() }?.let { gson.fromJson(it, T::class.java) }
                )
            } catch (jsonSyntaxException: JsonSyntaxException) {
                completion.onComplete(jsonSyntaxException, null)
            }
        }
    }
)

fun makeRequest(
    uri: String,
    body: RequestBody? = null,
    headers: Map<String, String>? = null,
    completion: HttpEmptyCompletion,
) = makeRequest(
    uri,
    body,
    headers,
    object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            completion.onComplete(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.takeIf { it.isSuccessful }?.let {
                completion.onComplete(null)
            } ?: completion.onComplete(
                ResponseException(ResponseException.Message.ERROR_RESPONSE).apply {
                    description = response.message
                }
            )
        }
    }
)

fun makeRequest(
    uri: String,
    body: RequestBody? = null,
    headers: Map<String, String>? = null,
    responseCallback: Callback,
) {
    val request = Request.Builder().url(uri).apply {
        body?.let { post(it) }
        headers?.let {
            for ((key, value) in it) {
                addHeader(key, value)
            }
        }
    }.build()
    httpClient.newCall(request).enqueue(responseCallback)
}
