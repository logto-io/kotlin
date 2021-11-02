package io.logto.android.auth.browser

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.QueryKey
import io.logto.android.exception.LogtoException
import io.logto.android.model.OidcConfiguration
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
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Mock
    private lateinit var logtoConfig: LogtoConfig

    @Mock
    private lateinit var logtoApiClient: LogtoApiClient

    @Mock
    private lateinit var onComplete: (exception: LogtoException?) -> Unit

    private lateinit var browserSignOutFlow: BrowserSignOutFlow

    private val logtoExceptionCaptor = argumentCaptor<LogtoException>()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.openMocks(this)
        `when`(logtoConfig.postLogoutRedirectUri).thenReturn("postLogoutRedirectUri")

        browserSignOutFlow = BrowserSignOutFlow(
            "idToken",
            logtoConfig,
            logtoApiClient,
            onComplete,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignOut() {
        val oidcConfiguration: OidcConfiguration = mock()
        `when`(oidcConfiguration.endSessionEndpoint).thenReturn("endSessionEndpoint")

        doAnswer {
            val block = it.arguments[0] as (OidcConfiguration) -> Unit
            block(oidcConfiguration)
            null
        }.`when`(logtoApiClient).discover(anyOrNull())

        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        browserSignOutFlow.start(activity)
        verify(activity).startActivity(anyOrNull())
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithInvalidUri() {
        val invalidUri: Uri = Utils.buildUriWithQueries(
            "invalidBaseUri",
            mapOf(
                QueryKey.ERROR_DESCRIPTION to "mocked sign out error description"
            )
        )
        browserSignOutFlow.onResult(invalidUri)
        verify(onComplete).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).hasMessageThat()
            .contains("mocked sign out error description")
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithEmptyUri() {
        val invalidUri: Uri = mock()
        browserSignOutFlow.onResult(invalidUri)
        verify(onComplete).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).hasMessageThat()
            .contains(LogtoException.UNKNOWN_ERROR)
    }

    @Test
    fun onResultShouldCompleteWithoutLogtoExceptionWithValidUri() {
        val validUri = Uri.parse(logtoConfig.postLogoutRedirectUri)
        browserSignOutFlow.onResult(validUri)
        verify(onComplete).invoke(logtoExceptionCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isNull()
    }
}
