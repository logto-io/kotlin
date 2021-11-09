package io.logto.android.auth.browser

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.EMPTY_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.INVALID_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.MISSING_AUTHORIZATION_CODE
import io.logto.client.exception.LogtoException.Companion.SIGN_IN_FAILED
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
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
import org.mockito.Mockito.anyString
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
    @Mock
    private lateinit var dummyOidcConfiguration: OidcConfiguration

    @Mock
    private lateinit var logtoConfigMock: LogtoConfig

    @Mock
    private lateinit var logtoAndroidClientMock: LogtoAndroidClient

    @Mock
    private lateinit var onCompleteMock: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit

    private lateinit var browserSignInFlow: BrowserSignInFlow

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val logtoExceptionCaptor = argumentCaptor<LogtoException>()
    private val tokenSetCaptor = argumentCaptor<TokenSet>()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.openMocks(this)

        `when`(logtoConfigMock.redirectUri).thenReturn("redirectUri")

        `when`(logtoAndroidClientMock.logtoConfig).thenReturn(logtoConfigMock)

        browserSignInFlow = BrowserSignInFlow(
            logtoAndroidClientMock,
            onCompleteMock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignIn() {
        doAnswer {
            val block = it.arguments[0] as (OidcConfiguration) -> Unit
            block(dummyOidcConfiguration)
            null
        }.`when`(logtoAndroidClientMock).getOidcConfiguration(anyOrNull())
        `when`(logtoAndroidClientMock.getSignInUrl(anyOrNull(), anyOrNull())).thenReturn("signInUrl")
        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        browserSignInFlow.start(activity)

        verify(activity).startActivity(anyOrNull())
    }

    @Test
    fun startShouldInvokeCompleteWithLogtoExceptionOnDiscoverFailed() {
        val mockLogtoException: LogtoException = mock()
        doAnswer {
            throw mockLogtoException
        }.`when`(logtoAndroidClientMock).getOidcConfiguration(anyOrNull())
        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        browserSignInFlow.start(activity)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isEqualTo(mockLogtoException)
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithEmptyUri() {
        val emptyUri = Uri.parse("")

        browserSignInFlow.onResult(emptyUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $EMPTY_REDIRECT_URI")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithUriContainsErrorDescription() {
        val invalidUri: Uri = mock()
        `when`(invalidUri.getQueryParameter(eq(QueryKey.ERROR_DESCRIPTION)))
            .thenReturn("mocked sign in error description")

        browserSignInFlow.onResult(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: mocked sign in error description")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithUriContainsError() {
        val invalidUri: Uri = mock()
        `when`(invalidUri.getQueryParameter(eq(QueryKey.ERROR))).thenReturn("mocked sign in error")

        browserSignInFlow.onResult(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: mocked sign in error")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithUriContainsInvalidRedirectUri() {
        val invalidUri = Uri.parse("invalidRedirectUri")

        browserSignInFlow.onResult(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $INVALID_REDIRECT_URI")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithUriWithoutAuthCode() {
        val invalidUri = Uri.parse(logtoConfigMock.redirectUri)

        browserSignInFlow.onResult(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $MISSING_AUTHORIZATION_CODE")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionWithUriContainsEmptyAuthCode() {
        val invalidUri = Utils.buildUriWithQueries(logtoConfigMock.redirectUri, mapOf(
            QueryKey.CODE to ""
        ))

        browserSignInFlow.onResult(invalidUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue)
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $MISSING_AUTHORIZATION_CODE")
        assertThat(tokenSetCaptor.firstValue).isNull()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun onResultShouldCompleteWithTokenSetWithValidUri() {
        val tokenSet: TokenSet = mock()
        doAnswer {
            val block = it.arguments[2] as (TokenSet) -> Unit
            block(tokenSet)
            null
        }.`when`(logtoAndroidClientMock).grantTokenByAuthorizationCode(
            anyString(),
            anyString(),
            anyOrNull(),
        )
        val validUri = Utils.buildUriWithQueries(logtoConfigMock.redirectUri, mapOf(
            QueryKey.CODE to "authorizationCode"
        ))

        browserSignInFlow.onResult(validUri)

        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isNull()
        assertThat(tokenSetCaptor.firstValue).isEqualTo(tokenSet)
    }

    @Test
    fun onResultShouldCompleteWithLogtoExceptionOnGrantTokenByAuthorizationCodeFailed() {
        val validUri = Utils.buildUriWithQueries(logtoConfigMock.redirectUri, mapOf(
            QueryKey.CODE to "authorizationCode"
        ))
        val mockLogtoException: LogtoException = mock()
        doAnswer {
            throw mockLogtoException
        }.`when`(logtoAndroidClientMock).grantTokenByAuthorizationCode(
            anyString(),
            anyString(),
            anyOrNull(),
        )
        browserSignInFlow.onResult(validUri)
        verify(onCompleteMock).invoke(logtoExceptionCaptor.capture(), tokenSetCaptor.capture())
        assertThat(logtoExceptionCaptor.firstValue).isEqualTo(mockLogtoException)
        assertThat(tokenSetCaptor.firstValue).isNull()
    }
}
