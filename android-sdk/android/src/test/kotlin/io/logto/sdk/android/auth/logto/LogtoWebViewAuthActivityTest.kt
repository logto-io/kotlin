package io.logto.sdk.android.auth.logto

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.R
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class LogtoWebViewAuthActivityTest {

    private lateinit var activityController: ActivityController<LogtoWebViewAuthActivity>
    private lateinit var activity: LogtoWebViewAuthActivity
    private val testUri = "https://logto.dev"

    @Before
    fun setUp() {
        val appContext: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(appContext, LogtoWebViewAuthActivity::class.java).apply {
            putExtra("EXTRA_URI", testUri)
        }
        activityController = Robolectric.buildActivity(LogtoWebViewAuthActivity::class.java, intent)
        activity = activityController.get().apply {
            setTheme(R.style.Theme_AppCompat)
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `the WebView should load specified uri in webView`() {
        activityController.create()

        assertThat(activity.webView).isNotNull()
        assertThat(activity.webView.url).isEqualTo(testUri)
    }

    @Test
    fun `the LogtoAuthManager should handle user cancel on destroy`() {
        mockkObject(LogtoAuthManager)
        every { LogtoAuthManager.handleUserCancel() } just Runs

        activityController.create().destroy()

        verify {
            LogtoAuthManager.handleUserCancel()
        }
    }
}
