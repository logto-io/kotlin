package io.logto.sdk.android.util

import com.google.common.truth.Truth.assertThat

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Test
import java.util.*

class LogtoUtilsTest {

    @Test
    fun `nowRoundToSec should return time in seconds`() {
        val testMillis = 10000L
        val calendarMock: Calendar = mockk()
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendarMock
        every { calendarMock.timeInMillis } returns testMillis
        assertThat(LogtoUtils.nowRoundToSec()).isEqualTo(10L)
    }

    @Test
    fun `expiresAtFrom should get expected expire time`() {
        val startTime = 1234L
        val lifeTime = 5678L
        val expiresAt = LogtoUtils.expiresAtFrom(startTime, lifeTime)
        assertThat(expiresAt).isEqualTo(startTime + lifeTime)
    }

    @Test
    fun `isDependencyInstalled should return true if dependency is installed`() {
        val installedDependency = "org.junit.Test"
        assertThat(LogtoUtils.isDependencyInstalled(installedDependency)).isTrue()
    }

    @Test
    fun `isDependencyInstalled should return false if dependency is not installed`() {
        val notInstalledDependencyIdentify = "notInstalled"
        assertThat(LogtoUtils.isDependencyInstalled(notInstalledDependencyIdentify)).isFalse()
    }
}
