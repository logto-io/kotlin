package io.logto.android.utils

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class UtilsTest {
    @Test
    fun buildUriWithQueries() {
        val baseUrl = "logto.io"
        val queries = mapOf(
            "key1" to "value1",
            "key2" to "value2",
        )

        val uri = Utils.buildUriWithQueries(baseUrl, queries)

        assertThat(uri.path).isEqualTo(baseUrl)
        assertThat(uri.getQueryParameter("key1")).isEqualTo("value1")
        assertThat(uri.getQueryParameter("key2")).isEqualTo("value2")
    }

    @Test
    fun validateRedirectUriShouldReturnExceptionMessageWithEmptyUri() {
        val dummyBaseUri = UUID.randomUUID().toString()
        val emptyUri = Uri.parse("")

        val exceptionMsg = Utils.validateRedirectUri(emptyUri, dummyBaseUri)

        assertThat(exceptionMsg).isEqualTo(LogtoException.EMPTY_REDIRECT_URI)
    }

    @Test
    fun validateRedirectUriShouldReturnExceptionMessageWithUriWithErrorDesc() {
        val dummyBaseUri = UUID.randomUUID().toString()
        val exceptionMsg = UUID.randomUUID().toString()
        val uriWithErrorDesc = Utils.buildUriWithQueries(dummyBaseUri, mapOf(
            QueryKey.ERROR_DESCRIPTION to exceptionMsg
        ))

        val expectedExceptionMsg = Utils.validateRedirectUri(uriWithErrorDesc, dummyBaseUri)

        assertThat(expectedExceptionMsg).isEqualTo(exceptionMsg)
    }

    @Test
    fun validateRedirectUriShouldReturnExceptionMessageWithUriWithError() {
        val dummyBaseUri = UUID.randomUUID().toString()
        val exceptionMsg = UUID.randomUUID().toString()
        val uriWithError = Utils.buildUriWithQueries(dummyBaseUri, mapOf(
            QueryKey.ERROR to exceptionMsg
        ))

        val expectedExceptionMsg = Utils.validateRedirectUri(uriWithError, dummyBaseUri)

        assertThat(expectedExceptionMsg).isEqualTo(exceptionMsg)
    }

    @Test
    fun validateRedirectUriShouldReturnExceptionMessageWithUriNotMatchBaseUri() {
        val baseUri = UUID.randomUUID().toString()
        val uri = Uri.parse(baseUri)
        val anotherBaseUri = UUID.randomUUID().toString()

        val expectedExceptionMsg = Utils.validateRedirectUri(uri, anotherBaseUri)

        assertThat(expectedExceptionMsg).isEqualTo(LogtoException.INVALID_REDIRECT_URI)
    }

    @Test
    fun validateRedirectUriShouldReturnNullWithValidUri() {
        val baseUri = UUID.randomUUID().toString()
        val validUri = Uri.parse(baseUri)

        val exceptionMsg = Utils.validateRedirectUri(validUri, baseUri)

        assertThat(exceptionMsg).isNull()
    }
}
