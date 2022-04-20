package io.logto.sdk.android.util

import com.google.common.truth.Truth.assertThat

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Test
import java.util.*

class LogtoUtilsTest {

    @Test
    fun nowRoundToSec() {
        val testMillis = 10000L
        val calendarMock: Calendar = mockk()
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendarMock
        every { calendarMock.timeInMillis } returns testMillis
        assertThat(LogtoUtils.nowRoundToSec()).isEqualTo(10L)
    }

    @Test
    fun expiresAtFrom() {
        val startTime = 1234L
        val lifeTime = 5678L
        val expiresAt = LogtoUtils.expiresAtFrom(startTime, lifeTime)
        assertThat(expiresAt).isEqualTo(startTime + lifeTime)
    }

    @Test
    fun isDependencyInstalled() {
        val installedDependency = "org.junit.Test"
        assertThat(LogtoUtils.isDependencyInstalled(installedDependency)).isTrue()

        val notInstalledDependencyIdentify = "notInstalled"
        assertThat(LogtoUtils.isDependencyInstalled(notInstalledDependencyIdentify)).isFalse()
    }
}
