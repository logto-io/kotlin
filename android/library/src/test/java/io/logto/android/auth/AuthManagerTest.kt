package io.logto.android.auth

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthManagerTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val testFlow = mock(IFlow::class.java)
    private val testUriData = mock(Uri::class.java)

    @Test
    fun authManagerOnStart() {
        AuthManager.start(context, testFlow)
        assertThat(AuthManager.currentFlow).isEqualTo(testFlow)
        verify(testFlow).start(context)

        AuthManager.handleRedirectUri(testUriData)
        verify(testFlow).handleRedirectUri(testUriData)

        AuthManager.reset()
        assertThat(AuthManager.currentFlow).isEqualTo(null)
    }

    @Test
    fun authManagerHandleRedirectUri() {
        AuthManager.start(context, testFlow)
        AuthManager.handleRedirectUri(testUriData)
        verify(testFlow).handleRedirectUri(testUriData)
    }

    @Test
    fun authManagerOnReset() {
        AuthManager.start(context, testFlow)
        AuthManager.reset()
        assertThat(AuthManager.currentFlow).isEqualTo(null)
    }
}
