package io.logto.client.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun expiresAtFrom() {
        val startTime = 1000L
        val lifeTime = 1000L
        val expectedExpiresAt = 2000L
        assertThat(TimeUtils.expiresAtFrom(startTime, lifeTime)).isEqualTo(expectedExpiresAt)
    }
}
