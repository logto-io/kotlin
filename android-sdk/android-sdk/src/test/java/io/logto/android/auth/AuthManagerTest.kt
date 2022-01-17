package io.logto.android.auth

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthManagerTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @RelaxedMockK
    private lateinit var testFlow: IFlow

    @RelaxedMockK
    private lateinit var testUriData: Uri

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        AuthManager.reset()
    }

    @Test
    fun authManagerOnStart() {
        AuthManager.start(context, testFlow)

        assertThat(AuthManager.currentFlow).isEqualTo(testFlow)
        verify { testFlow.start(context) }

        AuthManager.handleRedirectUri(testUriData)

        verify { testFlow.handleRedirectUri(testUriData) }

        AuthManager.reset()

        assertThat(AuthManager.currentFlow).isNull()
    }

    @Test
    fun authManagerHandleRedirectUri() {
        AuthManager.start(context, testFlow)
        AuthManager.handleRedirectUri(testUriData)

        verify { testFlow.handleRedirectUri(testUriData) }
    }

    @Test
    fun authManagerOnReset() {
        AuthManager.start(context, testFlow)
        AuthManager.reset()

        assertThat(AuthManager.currentFlow).isNull()
    }

    @Test
    fun authManagerHandleUserCanceled() {
        AuthManager.start(context, testFlow)
        AuthManager.handleUserCanceled()

        verify { testFlow.handleUserCanceled() }
        assertThat(AuthManager.currentFlow).isNull()
    }
}
