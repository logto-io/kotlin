package io.logto.android.auth.browser

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.QueryKey
import io.logto.android.exception.LogtoException
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BrowserSignInFlowTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Mock
    private lateinit var logtoConfig: LogtoConfig

    @Mock
    private lateinit var logtoApiClient: LogtoApiClient

    @Mock
    private lateinit var onComplete: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit

    private val logtoExceptionCaptor = argumentCaptor<LogtoException>()
    private val tokenSetCaptor = argumentCaptor<TokenSet>()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.openMocks(this)
        `when`(logtoConfig.redirectUri).thenReturn("redirectUri")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignIn() {
        val oidcConfiguration: OidcConfiguration = mock()
        `when`(oidcConfiguration.authorizationEndpoint).thenReturn("authorizationEndpoint")

        doAnswer {
            val block = it.arguments[0] as (OidcConfiguration) -> Unit
            block(oidcConfiguration)
            null
        }.`when`(logtoApiClient).discover(anyOrNull())

        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        val browserSignInFlow = BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
            onComplete
        )

        browserSignInFlow.start(activity)
        verify(activity).startActivity(anyOrNull())
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithInvalidUri() {
        val browserSignInFlow = BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
            onComplete,
        )

        val invalidUri: Uri = mock()
        `when`(invalidUri.getQueryParameter(eq(QueryKey.CODE))).thenReturn(null)
        `when`(invalidUri.getQueryParameter(eq(QueryKey.ERROR_DESCRIPTION)))
            .thenReturn("mocked sign in error description")

        browserSignInFlow.onResult(invalidUri)

        verify(onComplete).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .contains("mocked sign in error description")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithEmptyUri() {
        val browserSignInFlow = BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
            onComplete,
        )
        val empty: Uri = mock()
        browserSignInFlow.onResult(empty)
        verify(onComplete).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .contains(LogtoException.UNKNOWN_ERROR)
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun onResultShouldCompleteWithTokenSetWithValidUri() {
        val tokenSet: TokenSet = mock()

        doAnswer {
            val block = it.arguments[4] as (TokenSet) -> Unit
            block(tokenSet)
            null
        }.`when`(logtoApiClient).grantTokenByAuthorizationCode(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )

        val browserSignInFlow = BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
            onComplete,
        )

        val validUri = Utils.buildUriWithQueries(logtoConfig.redirectUri, mapOf(
            QueryKey.CODE to "authorizationCode"
        ))

        browserSignInFlow.onResult(validUri)

        verify(onComplete).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isNull()
        assertThat(tokenSetCaptor.firstValue).isEqualTo(tokenSet)
    }
}
