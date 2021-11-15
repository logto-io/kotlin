package io.logto.client.extensions

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.logto.client.exception.LogtoException

suspend inline fun <reified T> HttpClient.httpGet(urlString: String, exceptLogtoExceptionDesc: String): T {
    try {
        return get(urlString)
    } catch (exception: RuntimeException) {
        throw LogtoException(exceptLogtoExceptionDesc, exception)
    }
}

suspend inline fun <reified T> HttpClient.httpPost(
    urlString: String,
    exceptLogtoExceptionDesc: String,
    block: HttpRequestBuilder.() -> Unit,
): T {
    try {
        return post(urlString, block)
    } catch (exception: RuntimeException) {
        throw LogtoException(exceptLogtoExceptionDesc, exception)
    }
}
