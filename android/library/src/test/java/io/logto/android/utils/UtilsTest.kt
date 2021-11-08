package io.logto.android.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
        assertThat(uri.getQueryParameter("key1")).isEqualTo("value1")
        assertThat(uri.getQueryParameter("key2")).isEqualTo("value2")
    }
}
