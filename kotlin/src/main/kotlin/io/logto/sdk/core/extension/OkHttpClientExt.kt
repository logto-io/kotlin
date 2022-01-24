package io.logto.sdk.core.extension

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import io.logto.sdk.core.callback.HttpCompletion
import io.logto.sdk.core.callback.HttpEmptyCompletion
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

inline fun <reified T> OkHttpClient.get(
    uri: String,
    completion: HttpCompletion<T?>? = null
) = get(uri, null, completion)

inline fun <reified T> OkHttpClient.get(
    uri: String,
    headers: Map<String, String>? = null,
    completion: HttpCompletion<T?>? = null,
) {
    val request = Request
        .Builder()
        .url(uri)
        .apply {
            headers?.let { for ((key, value) in it) { addHeader(key, value) } }
        }.build()

    newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            completion?.onComplete(e, null)
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                if (!response.isSuccessful) {
                    throw ResponseException(
                        ResponseException.Message.ERROR_RESPONSE
                    )
                }
                completion?.onComplete(
                    null,
                    response
                        .let { it.body?.string() }
                        ?.let { gson.fromJson(it, T::class.java) }
                )
            } catch (jsonSyntaxException: JsonSyntaxException) {
                completion?.onComplete(jsonSyntaxException, null)
            } catch (responseException: ResponseException) {
                completion?.onComplete(responseException, null)
            }
        }
    })
}

inline fun <reified T> OkHttpClient.post(uri: String, body: RequestBody, completion: HttpCompletion<T?>? = null) =
    post(uri, body, null, completion)

inline fun <reified T> OkHttpClient.post(
    uri: String,
    body: RequestBody,
    headers: Map<String, String>? = null,
    completion: (HttpCompletion<T?>)? = null,
) {
    val request = Request
        .Builder()
        .url(uri)
        .post(body)
        .apply {
            headers?.let { for ((key, value) in it) { addHeader(key, value) } }
        }
        .build()

    newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            completion?.onComplete(e, null)
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                if (!response.isSuccessful) {
                    throw ResponseException(
                        ResponseException.Message.ERROR_RESPONSE
                    )
                }
                completion?.onComplete(
                    null,
                    response
                        .let { it.body?.string() }
                        ?.let { gson.fromJson(it, T::class.java) }
                )
            } catch (jsonSyntaxException: JsonSyntaxException) {
                completion?.onComplete(jsonSyntaxException, null)
            } catch (responseException: ResponseException) {
                completion?.onComplete(responseException, null)
            }
        }
    })
}

fun OkHttpClient.post(uri: String, body: RequestBody, completion: HttpEmptyCompletion? = null) =
    post(uri, body, null, completion)

fun OkHttpClient.post(
    uri: String,
    body: RequestBody,
    headers: Map<String, String>? = null,
    completion: (HttpEmptyCompletion)? = null,
) {
    val request = Request
        .Builder()
        .url(uri)
        .post(body)
        .apply {
            headers?.let { for ((key, value) in it) { addHeader(key, value) } }
        }
        .build()

    newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            completion?.onComplete(e)
        }

        override fun onResponse(call: Call, response: Response) {
            completion?.onComplete(
                if (!response.isSuccessful) {
                    ResponseException(ResponseException.Message.ERROR_RESPONSE)
                } else null
            )
        }
    })
}
