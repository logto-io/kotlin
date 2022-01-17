package io.logto.android.auth.browser

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.android.callback.HandleLogtoExceptionCallback
import io.logto.android.callback.HandleOidcConfigurationCallback
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.EMPTY_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.INVALID_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.SIGN_OUT_FAILED
import io.logto.client.exception.LogtoException.Companion.USER_CANCELED
import io.logto.client.model.OidcConfiguration
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BrowserSignOutFlowTest {
    @RelaxedMockK
    private lateinit var logtoConfigMock: LogtoConfig

    @RelaxedMockK
    private lateinit var logtoAndroidClientMock: LogtoAndroidClient

    @RelaxedMockK
    private lateinit var dummyOidcConfiguration: OidcConfiguration

    @RelaxedMockK
    private lateinit var onCompleteMock: HandleLogtoExceptionCallback

    private lateinit var browserSignOutFlow: BrowserSignOutFlow

    private val logtoExceptionCaptureList = mutableListOf<LogtoException?>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { logtoConfigMock.postLogoutRedirectUri } returns "postLogoutRedirectUri"
        every { logtoAndroidClientMock.logtoConfig } returns logtoConfigMock

        browserSignOutFlow = BrowserSignOutFlow(
            "idToken",
            logtoAndroidClientMock,
            onCompleteMock,
        )
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignOut() {
        every {
            logtoAndroidClientMock.getOidcConfigurationAsync(any())
        } answers {
            val block = args[0] as HandleOidcConfigurationCallback
            block(null, dummyOidcConfiguration)
            Job(null)
        }
        every { logtoAndroidClientMock.getSignOutUrl(any(), any()) } returns "signOutUrl"
        val activity: Activity = spyk(Robolectric.buildActivity(Activity::class.java).get())

        browserSignOutFlow.start(activity)

        verify { activity.startActivity(any()) }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun startShouldInvokeCompleteWithLogtoExceptionOnDiscoverFailed() {
        val mockLogtoException: LogtoException = mockk()
        every {
            logtoAndroidClientMock.getOidcConfigurationAsync(any())
        } answers {
            val block = args[0] as HandleOidcConfigurationCallback
            block(mockLogtoException, null)
            Job(null)
        }
        val activity: Activity = spyk(Robolectric.buildActivity(Activity::class.java).get())

        browserSignOutFlow.start(activity)

        verify { onCompleteMock.invoke(mockLogtoException) }
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithEmptyUri() {
        val invalidUri: Uri = Uri.parse("")

        browserSignOutFlow.handleRedirectUri(invalidUri)

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList)) }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_OUT_FAILED: $EMPTY_REDIRECT_URI")
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsErrorDescription() {
        val invalidUri: Uri = Utils.buildUriWithQueries(
            logtoConfigMock.postLogoutRedirectUri,
            mapOf(
                QueryKey.ERROR_DESCRIPTION to "mocked sign out error description"
            )
        )

        browserSignOutFlow.handleRedirectUri(invalidUri)

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList)) }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_OUT_FAILED: mocked sign out error description")
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsError() {
        val invalidUri: Uri = Utils.buildUriWithQueries(
            logtoConfigMock.postLogoutRedirectUri,
            mapOf(
                QueryKey.ERROR to "mocked sign out error"
            )
        )

        browserSignOutFlow.handleRedirectUri(invalidUri)

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList)) }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_OUT_FAILED: mocked sign out error")
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsInvalidRedirectUri() {
        val invalidUri: Uri = Uri.parse("invalidRedirectUri")

        browserSignOutFlow.handleRedirectUri(invalidUri)

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList)) }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_OUT_FAILED: $INVALID_REDIRECT_URI")
    }

    @Test
    fun handleRedirectUrlShouldCompleteWithoutLogtoExceptionWithValidUri() {
        val validUri = Uri.parse(logtoConfigMock.postLogoutRedirectUri)

        browserSignOutFlow.handleRedirectUri(validUri)

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList)) }
        assertThat(logtoExceptionCaptureList.last()).isNull()
    }

    @Test
    fun handleUserCanceledShouldCompleteWithLogtoException() {
        browserSignOutFlow.handleUserCanceled()

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList)) }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo(USER_CANCELED)
    }
}
