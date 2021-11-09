package io.logto.android.auth.browser

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.android.client.LogtoAndroidClient
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.EMPTY_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.INVALID_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.SIGN_OUT_FAILED
import io.logto.client.model.OidcConfiguration
import io.logto.android.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BrowserSignOutFlowTest {
    @Mock
    private lateinit var logtoConfigMock: LogtoConfig

    @Mock
    private lateinit var logtoAndroidClientMock: LogtoAndroidClient

    @Mock
    private lateinit var dummyOidcConfiguration: OidcConfiguration

    @Mock
    private lateinit var onCompleteMock: (exception: LogtoException?) -> Unit

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private lateinit var browserSignOutFlow: BrowserSignOutFlow

    private val logtoExceptionCaptor = argumentCaptor<LogtoException>()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.openMocks(this)

        `when`(logtoConfigMock.postLogoutRedirectUri).thenReturn("postLogoutRedirectUri")

        `when`(logtoAndroidClientMock.logtoConfig).thenReturn(logtoConfigMock)

        browserSignOutFlow = BrowserSignOutFlow(
            "idToken",
            logtoAndroidClientMock,
            onCompleteMock,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignOut() {
        doAnswer {
            val block = it.arguments[0] as (OidcConfiguration) -> Unit
            block(dummyOidcConfiguration)
            null
        }.`when`(logtoAndroidClientMock).getOidcConfiguration(anyOrNull())
        `when`(logtoAndroidClientMock.getSignOutUrl(anyOrNull(), anyOrNull())).thenReturn("signOutUrl")
        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        browserSignOutFlow.start(activity)

        verify(activity).startActivity(anyOrNull())
    }

    @Test
    fun startShouldInvokeCompleteWithLogtoExceptionOnDiscoverFailed() {
        val mockLogtoException: LogtoException = mock()
        doAnswer {
            throw mockLogtoException
        }.`when`(logtoAndroidClientMock).getOidcConfiguration(anyOrNull())
        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        browserSignOutFlow.start(activity)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isEqualTo(mockLogtoException)
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithEmptyUri() {
        val invalidUri: Uri = Uri.parse("")

        browserSignOutFlow.handleRedirectUri(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).hasMessageThat()
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

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).hasMessageThat()
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

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).hasMessageThat()
            .isEqualTo("$SIGN_OUT_FAILED: mocked sign out error")
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsInvalidRedirectUri() {
        val invalidUri: Uri = Uri.parse("invalidRedirectUri")

        browserSignOutFlow.handleRedirectUri(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).hasMessageThat()
            .isEqualTo("$SIGN_OUT_FAILED: $INVALID_REDIRECT_URI")
    }

    @Test
    fun handleRedirectUrlShouldCompleteWithoutLogtoExceptionWithValidUri() {
        val validUri = Uri.parse(logtoConfigMock.postLogoutRedirectUri)

        browserSignOutFlow.handleRedirectUri(validUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isNull()
    }
}
