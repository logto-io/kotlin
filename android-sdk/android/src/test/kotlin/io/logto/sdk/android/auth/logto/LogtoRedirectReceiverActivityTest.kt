package io.logto.sdk.android.auth.logto

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class LogtoRedirectReceiverActivityTest {

    private val callbackUri = Uri.parse("io.logto.android://io.logto.sample/callback?code=code&state=state")

    @Before
    fun setUp() {
        mockkObject(LogtoAuthManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        LogtoAuthManager.browserSession = null
    }

    private fun launchReceiver(uri: Uri?): LogtoRedirectReceiverActivity {
        val appContext: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(Intent.ACTION_VIEW, uri, appContext, LogtoRedirectReceiverActivity::class.java)
        return Robolectric.buildActivity(LogtoRedirectReceiverActivity::class.java, intent).create().get()
    }

    @Test
    fun `should forward the redirect uri to the browser auth activity and finish`() {
        every { LogtoAuthManager.isLogtoAuthResult(callbackUri) } returns true

        val activity = launchReceiver(callbackUri)

        val startedIntent = shadowOf(activity).nextStartedActivity
        assertThat(startedIntent.component?.className).isEqualTo(LogtoBrowserAuthActivity::class.java.name)
        assertThat(startedIntent.data).isEqualTo(callbackUri)
        assertThat(startedIntent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP).isNotEqualTo(0)
        assertThat(startedIntent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP).isNotEqualTo(0)
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `should drop a uri that does not match the pending session`() {
        every { LogtoAuthManager.isLogtoAuthResult(any()) } returns false

        val activity = launchReceiver(callbackUri)

        assertThat(shadowOf(activity).nextStartedActivity).isNull()
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `should drop an intent without a uri`() {
        val activity = launchReceiver(null)

        assertThat(shadowOf(activity).nextStartedActivity).isNull()
        assertThat(activity.isFinishing).isTrue()
    }
}
