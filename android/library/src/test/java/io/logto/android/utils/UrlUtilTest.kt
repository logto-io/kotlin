package io.logto.android.utils

import android.net.Uri
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlUtilTest {
    @Test
    fun getQueryParam_containsKey_returnValue() {
        val url = "http://localhost:3000/callback?code=k_iJIyf1qFt0Xnl_RPO6XUrs-QG-DEuPVxC68ugw9K-"
        val value = UrlUtil.getQueryParam(url, "code")
        assertThat(value, `is`("k_iJIyf1qFt0Xnl_RPO6XUrs-QG-DEuPVxC68ugw9K-"))
    }

    @Test
    fun getQueryParam_doesNotContainKey_returnNull() {
        val url = "http://localhost:3000/callback?code=k_iJIyf1qFt0Xnl_RPO6XUrs-QG-DEuPVxC68ugw9K-"
        val value = UrlUtil.getQueryParam(url, "no_code")
        assertThat(value, nullValue())
    }

    @Test
    fun appendQueryParameters_withKV_shouldAppend() {
        val baseUrl = Uri.parse("https://www.example.com")
        val url = UrlUtil.appendQueryParameters(
            baseUrl.buildUpon(),
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3",
            )
        ).toString()
        assertThat(UrlUtil.getQueryParam(url, "key1"), `is`("value1"))
        assertThat(UrlUtil.getQueryParam(url, "key2"), `is`("value2"))
        assertThat(UrlUtil.getQueryParam(url, "key3"), `is`("value3"))
    }

    @Test
    fun appendQueryParameters_withoutKV_shouldNotAppend() {
        val baseUrl = Uri.parse("https://www.example.com")
        val url = UrlUtil.appendQueryParameters(baseUrl.buildUpon(), mapOf()).toString()
        assertThat(baseUrl.toString(), `is`(url))
    }
}
