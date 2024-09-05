package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.type.SignInOptions
import io.logto.sdk.core.Core
import io.logto.sdk.core.exception.CallbackUriVerificationException
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.GenerateSignInUriOptions
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.util.CallbackUriUtils
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoAuthSessionTest {

    private val mockActivity: Activity = mockk()
    private val dummyOidcConfigResponse = OidcConfigResponse(
        authorizationEndpoint = "authorizationEndpoint",
        tokenEndpoint = "tokenEndpoint",
        endSessionEndpoint = "endSessionEndpoint",
        userinfoEndpoint = "userinfoEndpoint",
        jwksUri = "jwksUri",
        issuer = "issuer",
        revocationEndpoint = "revocationEndpoint",
    )

    private val dummyLogtoConfig = LogtoConfig(
        "endpoint",
        "appId",
    )

    private val dummyRedirectUri = "localhost:3001/callback"

    private val dummySignInOptions = SignInOptions(
        redirectUri = dummyRedirectUri
    )

    @Before
    fun setUp() {
        every { mockActivity.packageName } returns "logto.test"

        every { mockActivity.startActivity(any()) } just Runs

        mockkObject(LogtoAuthManager)
        mockkObject(Core)
    }

    @Test
    fun `start should register to the logto auth manager and start an activity`() {
        val mockCompletion: Completion<LogtoException, CodeTokenResponse> = mockk()

        every {
            Core.generateSignInUri(any())
        } returns "testSignInUri"

        val logtoAuthSession = LogtoAuthSession(
            mockActivity,
            dummyLogtoConfig,
            dummyOidcConfigResponse,
            dummySignInOptions,
            mockCompletion
        )

        logtoAuthSession.start()

        verify {
            LogtoAuthManager.handleAuthStart(logtoAuthSession)
        }

        verify {
            mockActivity.startActivity(any())
        }
    }

    @Test
    fun `should complete with exception with invalid redirectUri`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val codeTokenResponseCapture = mutableListOf<CodeTokenResponse?>()

        mockkObject(LogtoAuthManager)
        val mockLogtoConfig: LogtoConfig = mockk()
        val invalidRedirectUri = ""
        val mockCompletion: Completion<LogtoException, CodeTokenResponse> = mockk()
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val logtoAuthSession = LogtoAuthSession(
            mockActivity,
            mockLogtoConfig,
            dummyOidcConfigResponse,
            SignInOptions(redirectUri = invalidRedirectUri),
            mockCompletion,
        )

        logtoAuthSession.start()

        verify {
            mockCompletion.onComplete(
                captureNullable(logtoExceptionCapture),
                captureNullable(codeTokenResponseCapture),
            )
        }

        assertThat(logtoExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.INVALID_REDIRECT_URI.name)
        assertThat(codeTokenResponseCapture.last()).isNull()

        verify(exactly = 0) {
            LogtoAuthManager.handleAuthStart(logtoAuthSession)
            Core.generateSignInUri(any())
            mockActivity.startActivity(any())
        }
    }

    @Test
    fun `handleCallbackUri should complete with expected results`() {
        val mockCompletion: Completion<LogtoException, CodeTokenResponse> = mockk()
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val logtoAuthSession = LogtoAuthSession(
            mockActivity,
            dummyLogtoConfig,
            dummyOidcConfigResponse,
            dummySignInOptions,
            mockCompletion,
        )

        val testCode = "testCode"
        val dummyValidCallbackUri = Uri.parse("$dummyRedirectUri?code=$testCode&state=dummystate")
        val mockCodeTokenResponse: CodeTokenResponse = mockk()
        mockkObject(CallbackUriUtils)
        every {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(
                any(),
                any(),
                any()
            )
        } returns testCode

        every {
            Core.fetchTokenByAuthorizationCode(any(), any(), any(), any(), any(), any(), any())
        } answers {
            lastArg<HttpCompletion<CodeTokenResponse>>().onComplete(null, mockCodeTokenResponse)
        }

        logtoAuthSession.handleCallbackUri(dummyValidCallbackUri)

        verify {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(any(), any(), any())
            Core.fetchTokenByAuthorizationCode(any(), any(), any(), any(), any(), any(), any())
            mockCompletion.onComplete(null, mockCodeTokenResponse)
        }
    }

    @Test
    fun `handleCallbackUri should complete with exception if the callbackUri is invalid`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val codeTokenResponseCapture = mutableListOf<CodeTokenResponse?>()

        val mockCompletion: Completion<LogtoException, CodeTokenResponse> = mockk()
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val logtoAuthSession = LogtoAuthSession(
            mockActivity,
            dummyLogtoConfig,
            dummyOidcConfigResponse,
            dummySignInOptions,
            mockCompletion,
        )

        mockkObject(CallbackUriUtils)
        every {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(any(), any(), any())
        } throws CallbackUriVerificationException(CallbackUriVerificationException.Type.ERROR_FOUND_IN_URI)

        val dummyInvalidCallbackUri: Uri = mockk()
        every { dummyInvalidCallbackUri.toString() } returns "dummyCallbackUri"

        logtoAuthSession.handleCallbackUri(dummyInvalidCallbackUri)

        verify {
            mockCompletion.onComplete(
                captureNullable(logtoExceptionCapture),
                captureNullable(codeTokenResponseCapture)
            )
        }

        assertThat(logtoExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.INVALID_CALLBACK_URI.name)
        assertThat(codeTokenResponseCapture.last()).isNull()
    }

    @Test
    fun `handleCallbackUri should complete with exception if can not fetch token by authorization code`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val codeTokenResponseCapture = mutableListOf<CodeTokenResponse?>()

        val mockCompletion: Completion<LogtoException, CodeTokenResponse> = mockk()
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val testCode = "testCode"
        val dummyValidCallbackUri = Uri.parse("$dummyRedirectUri?code=$testCode&state=dummystate")
        mockkObject(CallbackUriUtils)
        every {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(
                any(),
                any(),
                any()
            )
        } returns testCode

        val mockFetchException: Throwable = mockk()
        every {
            Core.fetchTokenByAuthorizationCode(any(), any(), any(), any(), any(), any(), any())
        } answers {
            lastArg<HttpCompletion<CodeTokenResponse>>().onComplete(mockFetchException, null)
        }

        val logtoAuthSession = LogtoAuthSession(
            mockActivity,
            dummyLogtoConfig,
            dummyOidcConfigResponse,
            dummySignInOptions,
            mockCompletion,
        )

        logtoAuthSession.handleCallbackUri(dummyValidCallbackUri)

        verify {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(any(), any(), any())
            Core.fetchTokenByAuthorizationCode(any(), any(), any(), any(), any(), any(), any())
        }

        verify {
            mockCompletion.onComplete(
                captureNullable(logtoExceptionCapture),
                captureNullable(codeTokenResponseCapture),
            )
        }

        assertThat(logtoExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.UNABLE_TO_FETCH_TOKEN_BY_AUTHORIZATION_CODE.name)
        assertThat(codeTokenResponseCapture.last()).isNull()
    }

    @Test
    fun `handleUserCancel should complete with user canceled exception`() {
        val logtoExceptionCapture = mutableListOf<LogtoException?>()
        val codeTokenResponseCapture = mutableListOf<CodeTokenResponse?>()

        val mockCompletion: Completion<LogtoException, CodeTokenResponse> = mockk()
        every { mockCompletion.onComplete(any(), any()) } just Runs

        val logtoAuthSession = LogtoAuthSession(
            mockActivity,
            dummyLogtoConfig,
            dummyOidcConfigResponse,
            dummySignInOptions,
            mockCompletion,
        )

        logtoAuthSession.handleUserCancel()

        verify {
            mockCompletion.onComplete(
                captureNullable(logtoExceptionCapture),
                captureNullable(codeTokenResponseCapture),
            )
        }

        assertThat(logtoExceptionCapture.last())
            .hasMessageThat()
            .isEqualTo(LogtoException.Type.USER_CANCELED.name)
        assertThat(codeTokenResponseCapture.last()).isNull()
    }
}
