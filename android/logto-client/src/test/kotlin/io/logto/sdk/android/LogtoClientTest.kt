package io.logto.sdk.android

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.callback.RetrieveCallback
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.util.LogtoUtils
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.IdTokenClaims
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.util.TokenUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.jose4j.jwt.consumer.InvalidJwtException
import org.junit.Test

class LogtoClientTest {
    private val oidcConfigResponseMock: OidcConfigResponse = mockk()
    private val logtoConfigMock: LogtoConfig = mockk()
    private lateinit var logtoClient: LogtoClient

    companion object {
        private const val TEST_SCOPE_1 = "scope_1"
        private const val TEST_SCOPE_2 = "scope_2"
        private const val TEST_SCOPE_3 = "scope_3"

        private const val TEST_RESOURCE_1 = "resource_1"
        private const val TEST_RESOURCE_2 = "resource_2"
        private const val TEST_RESOURCE_3 = "resource_3"

        private const val TEST_CLIENT_ID = "client_id"
        private const val TEST_REFRESH_TOKEN = "refreshToken"
        private const val TEST_TOKEN_ENDPOINT = "tokenEndpoint"
        private const val TEST_ACCESS_TOKEN = "accessToken"
        private const val TEST_ID_TOKEN = "idToken"
        private const val TEST_EXPIRE_IN = 60L
    }

    @Test
    fun `getAccessToken should fail without being authenticated`() {
        logtoClient = LogtoClient(logtoConfigMock)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns false

        logtoClient.getAccessToken { throwable, result ->
            assertThat(throwable).hasMessageThat().contains(LogtoException.Message.NOT_AUTHENTICATED.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail without refreshToken`() {

        every { logtoConfigMock.scope } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)

        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupRefreshToken(null)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken { throwable, result ->
            assertThat(throwable).hasMessageThat().contains(LogtoException.Message.MISSING_REFRESH_TOKEN.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail when scopes are not all granted`() {

        every { logtoConfigMock.scope } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)

        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken(
            resource = null,
            scope = listOf(TEST_SCOPE_2, TEST_SCOPE_3)
        ) { throwable, result ->
            assertThat(throwable)
                .hasMessageThat()
                .contains(LogtoException.Message.SCOPES_ARE_NOT_ALL_GRANTED.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail when resource is not granted`() {

        every { logtoConfigMock.resource } returns listOf(TEST_RESOURCE_1, TEST_RESOURCE_2)

        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken(
            resource = TEST_RESOURCE_3,
            scope = null
        ) { throwable, result ->
            assertThat(throwable)
                .hasMessageThat()
                .contains(LogtoException.Message.RESOURCE_IS_NOT_GRANTED.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should return valid accessToken which is already exist`() {

        every { logtoConfigMock.scope } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)

        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)

        val testTokenKey = logtoClient.buildAccessTokenKey(listOf(TEST_SCOPE_1), null)
        val testAccessToken: AccessToken = mockk()
        every { testAccessToken.expiresAt } returns LogtoUtils.nowRoundToSec() + 1L

        logtoClient.setupAccessTokenMap(mapOf(testTokenKey to testAccessToken))

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken(
            null,
            listOf(TEST_SCOPE_1)
        ) { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isEqualTo(testAccessToken)
        }
    }

    @Test
    fun `getAccessToken should refresh token when existing accessToken is expired`() {
        setupRefreshTokenTestEnv()

        val expiredAccessTokenKey = logtoClient.buildAccessTokenKey(listOf(TEST_SCOPE_1), null)
        val expiredAccessToken: AccessToken = mockk()
        every { expiredAccessToken.expiresAt } returns LogtoUtils.nowRoundToSec() - 1L
        logtoClient.setupAccessTokenMap(mapOf(expiredAccessTokenKey to expiredAccessToken))

        logtoClient.getAccessToken(
            null,
            listOf(TEST_SCOPE_1)
        ) { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isNotNull()
            requireNotNull(result).apply {
                assertThat(token).isEqualTo(TEST_ACCESS_TOKEN)
                assertThat(scope).isEqualTo(TEST_SCOPE_1)
            }
        }

        verify(exactly = 1) {
            Core.fetchTokenByRefreshToken(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `getAccessToken should refresh token when accessToken does not exist`() {
        setupRefreshTokenTestEnv()

        logtoClient.getAccessToken(
            null,
            listOf(TEST_SCOPE_1)
        ) { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isNotNull()
            requireNotNull(result).apply {
                assertThat(token).isEqualTo(TEST_ACCESS_TOKEN)
                assertThat(scope).isEqualTo(TEST_SCOPE_1)
            }
        }

        verify(exactly = 1) {
            Core.fetchTokenByRefreshToken(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun getOidcConfig() {
        val logtoConfigMock: LogtoConfig = mockk()
        every { logtoConfigMock.endpoint } returns "https://logto.dev"

        mockkObject(Core)
        every { Core.fetchOidcConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val logtoClient = LogtoClient(logtoConfigMock)

        logtoClient.getOidcConfig { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isEqualTo(oidcConfigResponseMock)
        }
    }

    @Test
    fun `getOidcConfig success more than one time should only fetch once`() {
        val logtoConfigMock: LogtoConfig = mockk()
        every { logtoConfigMock.endpoint } returns "https://logto.dev"

        mockkObject(Core)
        every { Core.fetchOidcConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val logtoClient = LogtoClient(logtoConfigMock)

        logtoClient.getOidcConfig { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isEqualTo(oidcConfigResponseMock)
        }

        logtoClient.getOidcConfig { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isEqualTo(oidcConfigResponseMock)
        }

        verify(exactly = 1) { Core.fetchOidcConfig(any(), any()) }
    }

    @Test
    fun getIdTokenClaims() {
        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupIdToken(TEST_ID_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        val idTokenClaimsMock: IdTokenClaims = mockk()

        mockkObject(TokenUtils)
        every { TokenUtils.decodeIdToken(any()) } returns idTokenClaimsMock

        logtoClient.getIdTokenClaims { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isEqualTo(idTokenClaimsMock)
        }
    }

    @Test
    fun `getIdTokenClaims should fail without being authenticated`() {
        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupIdToken(TEST_ID_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns false

        logtoClient.getIdTokenClaims(object : RetrieveCallback<IdTokenClaims> {
            override fun onResult(throwable: Throwable?, result: IdTokenClaims?) {
                assertThat(throwable)
                    .hasMessageThat()
                    .contains(LogtoException.Message.NOT_AUTHENTICATED.name)
                assertThat(result).isNull()
            }
        })
    }

    @Test
    fun `getIdTokenClaims should fail if decodeIdToken failed`() {
        logtoClient = LogtoClient(logtoConfigMock)
        logtoClient.setupIdToken(TEST_ID_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        val invalidJwtExceptionMock: InvalidJwtException = mockk()

        mockkObject(TokenUtils)
        every { TokenUtils.decodeIdToken(any()) } throws invalidJwtExceptionMock

        logtoClient.getIdTokenClaims(object : RetrieveCallback<IdTokenClaims> {
            override fun onResult(throwable: Throwable?, result: IdTokenClaims?) {
                assertThat(throwable).isEqualTo(invalidJwtExceptionMock)
                assertThat(result).isNull()
            }
        })
    }

    private fun setupRefreshTokenTestEnv() {
        every { logtoConfigMock.scope } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)
        every { logtoConfigMock.clientId } returns TEST_CLIENT_ID

        logtoClient = LogtoClient(logtoConfigMock)

        mockkObject(logtoClient)
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)
        every { logtoClient.isAuthenticated } returns true

        every { oidcConfigResponseMock.tokenEndpoint } returns TEST_TOKEN_ENDPOINT
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<RetrieveCallback<OidcConfigResponse>>().onResult(null, oidcConfigResponseMock)
        }

        val refreshTokenTokenResponseMock: RefreshTokenTokenResponse = mockk()
        every { refreshTokenTokenResponseMock.accessToken } returns TEST_ACCESS_TOKEN
        every { refreshTokenTokenResponseMock.scope } returns TEST_SCOPE_1
        every { refreshTokenTokenResponseMock.expiresIn } returns TEST_EXPIRE_IN
        every { refreshTokenTokenResponseMock.refreshToken } returns TEST_REFRESH_TOKEN
        every { refreshTokenTokenResponseMock.idToken } returns TEST_ID_TOKEN

        mockkObject(Core)
        every { Core.fetchTokenByRefreshToken(any(), any(), any(), any(), any(), any()) } answers {
            lastArg<HttpCompletion<RefreshTokenTokenResponse>>()
                .onComplete(null, refreshTokenTokenResponseMock)
        }
    }
}
