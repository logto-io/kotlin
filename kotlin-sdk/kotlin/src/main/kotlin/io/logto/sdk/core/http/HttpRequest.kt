package io.logto.sdk.core.http

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import io.logto.sdk.core.exception.ResponseException
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

inline fun <reified T : Any> makeRequest(
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
                    ResponseException(ResponseException.Message.REQUEST_FAILED).apply {
                        responseMessage = response.message
                        responseContent = response.body?.toString()
                    },
                    null
                )
                return
            }

            response.let { it.body?.string() }?.let {
                try {
                    completion.onComplete(null, gson.fromJson(it, T::class.java))
                } catch (jsonSyntaxException: JsonSyntaxException) {
                    completion.onComplete(
                        ResponseException(ResponseException.Message.ERROR_RESPONSE, jsonSyntaxException),
                        null
                    )
                }
            } ?: completion.onComplete(ResponseException(ResponseException.Message.EMPTY_RESPONSE), null)
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
                ResponseException(ResponseException.Message.REQUEST_FAILED).apply {
                    responseMessage = response.message
                    responseContent = response.body?.toString()
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
