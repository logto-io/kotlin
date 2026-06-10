package io.logto.sdk.android.auth.logto

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.exception.LogtoException
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class LogtoBrowserAuthActivityTest {

    private lateinit var activityController: ActivityController<LogtoBrowserAuthActivity>
    private lateinit var activity: LogtoBrowserAuthActivity

    private val testAuthUri = "https://logto.dev/oidc/auth"
    private val testCallbackUri = Uri.parse("io.logto.android://io.logto.sample/callback?code=code&state=state")

    @Before
    fun setUp() {
        val appContext: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(appContext, LogtoBrowserAuthActivity::class.java).apply {
            putExtra("EXTRA_AUTH_URI", testAuthUri)
        }
        activityController = Robolectric.buildActivity(LogtoBrowserAuthActivity::class.java, intent)
        activity = activityController.get()
        mockkObject(LogtoAuthManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        LogtoAuthManager.browserSession = null
    }

    @Test
    fun `the first resume should launch the auth uri in a custom tab`() {
        activityController.create().resume()

        val startedIntent = shadowOf(activity).nextStartedActivity
        assertThat(startedIntent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(startedIntent.data).isEqualTo(Uri.parse(testAuthUri))
        assertThat(startedIntent.hasExtra("android.support.customtabs.extra.SESSION")).isTrue()
        assertThat(
            startedIntent.getIntExtra("androidx.browser.customtabs.extra.SHARE_STATE", 0),
        ).isEqualTo(2)
        assertThat(activity.isFinishing).isFalse()
    }

    @Test
    fun `should finish without launching anything if no auth uri is provided`() {
        val controller = Robolectric.buildActivity(LogtoBrowserAuthActivity::class.java)
        controller.create().resume()

        assertThat(shadowOf(controller.get()).nextStartedActivity).isNull()
        assertThat(controller.get().isFinishing).isTrue()
    }

    @Test
    fun `the LogtoAuthManager should handle the callback uri when the redirect is delivered`() {
        every { LogtoAuthManager.isLogtoAuthResult(testCallbackUri) } returns true
        every { LogtoAuthManager.handleCallbackUri(testCallbackUri) } just Runs

        activityController.create().resume()
        activityController.pause()

        val redirectIntent = LogtoBrowserAuthActivity.createRedirectHandlingIntent(activity, testCallbackUri)
        activityController.newIntent(redirectIntent).resume()

        verify {
            LogtoAuthManager.handleCallbackUri(testCallbackUri)
        }
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `the LogtoAuthManager should handle user cancel when resumed without a redirect`() {
        every { LogtoAuthManager.handleUserCancel() } just Runs

        activityController.create().resume()
        activityController.pause().resume()

        verify {
            LogtoAuthManager.handleUserCancel()
        }
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `the LogtoAuthManager should handle user cancel when the delivered uri is not an auth result`() {
        every { LogtoAuthManager.isLogtoAuthResult(any()) } returns false
        every { LogtoAuthManager.handleUserCancel() } just Runs

        activityController.create().resume()
        activityController.pause()

        val redirectIntent = LogtoBrowserAuthActivity.createRedirectHandlingIntent(
            activity,
            Uri.parse("unknown://uri"),
        )
        activityController.newIntent(redirectIntent).resume()

        verify {
            LogtoAuthManager.handleUserCancel()
        }
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `the LogtoAuthManager should handle failure if no browser can be launched`() {
        shadowOf(ApplicationProvider.getApplicationContext<Application>()).checkActivities(true)
        every { LogtoAuthManager.handleFailure(any()) } just Runs

        activityController.create().resume()

        val exceptionCapture = slot<LogtoException>()
        verify {
            LogtoAuthManager.handleFailure(capture(exceptionCapture))
        }
        assertThat(exceptionCapture.captured)
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER.name)
        assertThat(activity.isFinishing).isTrue()
    }
}
