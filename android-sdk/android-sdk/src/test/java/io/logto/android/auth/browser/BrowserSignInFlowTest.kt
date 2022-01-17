package io.logto.android.auth.browser

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.android.callback.HandleOidcConfigurationCallback
import io.logto.android.callback.HandleTokenSetCallback
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.EMPTY_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.INVALID_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.MISSING_AUTHORIZATION_CODE
import io.logto.client.exception.LogtoException.Companion.MISSING_STATE
import io.logto.client.exception.LogtoException.Companion.SIGN_IN_FAILED
import io.logto.client.exception.LogtoException.Companion.UNKNOWN_STATE
import io.logto.client.exception.LogtoException.Companion.USER_CANCELED
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import io.logto.client.utils.GenerateUtils
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BrowserSignInFlowTest {
    @RelaxedMockK
    private lateinit var dummyOidcConfiguration: OidcConfiguration

    @RelaxedMockK
    private lateinit var logtoConfigMock: LogtoConfig

    @RelaxedMockK
    private lateinit var logtoAndroidClientMock: LogtoAndroidClient

    @RelaxedMockK
    private lateinit var onCompleteMock: HandleTokenSetCallback

    private lateinit var browserSignInFlow: BrowserSignInFlow

    private val code = "authorizationCode"

    private val state = "state1"

    private val logtoExceptionCaptureList = mutableListOf<LogtoException?>()

    private val tokenSetCaptureList = mutableListOf<TokenSet?>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { logtoConfigMock.redirectUri } returns "redirectUri"
        every { logtoAndroidClientMock.logtoConfig } returns logtoConfigMock

        mockkObject(GenerateUtils)
        every { GenerateUtils.generateState() } returns state

        browserSignInFlow = BrowserSignInFlow(
            logtoAndroidClientMock,
            onCompleteMock
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignIn() {
        coEvery {
            logtoAndroidClientMock.getOidcConfigurationAsync(any())
        } coAnswers {
            val block = args[0] as HandleOidcConfigurationCallback
            block(null, dummyOidcConfiguration)
            Job(null)
        }

        every {
            logtoAndroidClientMock.getSignInUrl(any(), any(), any())
        } returns "signInUrl"

        val activity: Activity = spyk(Robolectric.buildActivity(Activity::class.java).get())
        browserSignInFlow.start(activity)

        verify { activity.startActivity(any()) }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun startShouldInvokeCompleteWithLogtoExceptionOnDiscoverFailed() {
        val mockLogtoException: LogtoException = mockk()
        coEvery {
            logtoAndroidClientMock.getOidcConfigurationAsync(any())
        } coAnswers {
            val block = args[0] as HandleOidcConfigurationCallback
            block(mockLogtoException, null)
            Job(null)
        }

        val activity: Activity = spyk(Robolectric.buildActivity(Activity::class.java).get())
        browserSignInFlow.start(activity)

        verify { onCompleteMock(mockLogtoException, captureNullable(tokenSetCaptureList)) }
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithEmptyUri() {
        val emptyUri = Uri.parse("")
        browserSignInFlow.handleRedirectUri(emptyUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $EMPTY_REDIRECT_URI")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsErrorDescription() {
        val invalidUri: Uri = mockk()
        every {
            invalidUri.getQueryParameter(eq(QueryKey.ERROR_DESCRIPTION))
        } returns "mocked sign in error description"

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: mocked sign in error description")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsError() {
        val invalidUri: Uri = mockk()
        every { invalidUri.getQueryParameter(eq(QueryKey.ERROR_DESCRIPTION)) } returns null
        every { invalidUri.getQueryParameter(eq(QueryKey.ERROR)) } returns "mocked sign in error"

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: mocked sign in error")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsInvalidRedirectUri() {
        val invalidUri = Uri.parse("invalidRedirectUri")

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $INVALID_REDIRECT_URI")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriWithoutState() {
        val invalidUri = Utils.buildUriWithQueries(
            logtoConfigMock.redirectUri, mapOf(
                QueryKey.CODE to code
            )
        )

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $MISSING_STATE")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriWithUnknownState() {
        val invalidUri = Utils.buildUriWithQueries(
            logtoConfigMock.redirectUri, mapOf(
                QueryKey.CODE to code,
                QueryKey.STATE to "unknownState"
            )
        )

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $UNKNOWN_STATE")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriWithoutAuthCode() {
        val invalidUri = Utils.buildUriWithQueries(
            logtoConfigMock.redirectUri, mapOf(
                QueryKey.STATE to state
            )
        )

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $MISSING_AUTHORIZATION_CODE")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleRedirectUriShouldCompleteWithLogtoExceptionWithUriContainsEmptyAuthCode() {
        val invalidUri = Utils.buildUriWithQueries(
            logtoConfigMock.redirectUri, mapOf(
                QueryKey.CODE to "",
                QueryKey.STATE to state,
            )
        )

        browserSignInFlow.handleRedirectUri(invalidUri)

        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last())
            .hasMessageThat()
            .isEqualTo("$SIGN_IN_FAILED: $MISSING_AUTHORIZATION_CODE")
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun handleRedirectUriShouldCompleteWithTokenSetWithValidUri() {
        val tokenSet: TokenSet = mockk()

        coEvery {
            logtoAndroidClientMock.grantTokenByAuthorizationCodeAsync(any(), any(), any())
        } coAnswers {
            val block = args[2] as HandleTokenSetCallback
            block(null, tokenSet)
            Job(null)
        }

        val validUri = Utils.buildUriWithQueries(
            logtoConfigMock.redirectUri, mapOf(
                QueryKey.CODE to code,
                QueryKey.STATE to state,
            )
        )
        browserSignInFlow.handleRedirectUri(validUri)

        verify { onCompleteMock.invoke(captureNullable(logtoExceptionCaptureList), tokenSet) }
        assertThat(logtoExceptionCaptureList.last()).isNull()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun handleRedirectUriShouldCompleteWithLogtoExceptionOnGrantTokenByAuthorizationCodeFailed() {
        val mockLogtoException: LogtoException = mockk()
        coEvery {
            logtoAndroidClientMock.grantTokenByAuthorizationCodeAsync(any(), any(), any())
        } coAnswers {
            val block = args[2] as HandleTokenSetCallback
            block(mockLogtoException, null)
            Job(null)
        }

        val validUri = Utils.buildUriWithQueries(
            logtoConfigMock.redirectUri, mapOf(
                QueryKey.CODE to code,
                QueryKey.STATE to state,
            )
        )
        browserSignInFlow.handleRedirectUri(validUri)

        verify { onCompleteMock.invoke(mockLogtoException, captureNullable(tokenSetCaptureList)) }
        assertThat(tokenSetCaptureList.last()).isNull()
    }

    @Test
    fun handleUserCanceledShouldCompleteWithLogtoException() {
        browserSignInFlow.handleUserCanceled()
        verify {
            onCompleteMock.invoke(
                captureNullable(logtoExceptionCaptureList),
                captureNullable(tokenSetCaptureList)
            )
        }
        assertThat(logtoExceptionCaptureList.last()).hasMessageThat().isEqualTo(USER_CANCELED)
        assertThat(tokenSetCaptureList.last()).isNull()
    }
}
