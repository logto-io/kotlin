package io.logto.sdk.android

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.util.LogtoUtils
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.IdTokenClaims
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.type.UserInfoResponse
import io.logto.sdk.core.util.TokenUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.jose4j.jwt.consumer.InvalidJwtException
import org.junit.Before
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
        private const val TEST_USERINFO_ENDPOINT = "userinfoEndpoint"
        private const val TEST_ACCESS_TOKEN = "accessToken"
        private const val TEST_ID_TOKEN = "idToken"
        private const val TEST_EXPIRE_IN = 60L
    }

    @Before
    fun setup() {
        // Note: Disable persist storage temporarily
        // TODO - Android Test Env Setup : LOG-1086
        every { logtoConfigMock.usingPersistStorage } returns false
    }

    @Test
    fun `getAccessToken should fail without being authenticated`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns false

        logtoClient.getAccessToken { logtoException, result ->
            assertThat(logtoException).hasMessageThat().contains(LogtoException.Message.NOT_AUTHENTICATED.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail without refreshToken`() {

        every { logtoConfigMock.scopes } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)

        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupRefreshToken(null)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken { logtoException, result ->
            assertThat(logtoException).hasMessageThat().contains(LogtoException.Message.NO_REFRESH_TOKEN_FOUND.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail when scopes are not all granted`() {

        every { logtoConfigMock.scopes } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)

        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken(
            resource = null,
            scopes = listOf(TEST_SCOPE_2, TEST_SCOPE_3)
        ) { logtoException, result ->
            assertThat(logtoException)
                .hasMessageThat()
                .contains(LogtoException.Message.UNGRANTED_SCOPE_FOUND.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail when resource is not granted`() {

        every { logtoConfigMock.resources } returns listOf(TEST_RESOURCE_1, TEST_RESOURCE_2)

        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken(
            resource = TEST_RESOURCE_3,
            scopes = null
        ) { logtoException, result ->
            assertThat(logtoException)
                .hasMessageThat()
                .contains(LogtoException.Message.UNGRANTED_RESOURCE_FOUND.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should return valid accessToken which is already exist`() {

        every { logtoConfigMock.scopes } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)

        logtoClient = LogtoClient(logtoConfigMock, mockk())
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
        ) { logtoException, result ->
            assertThat(logtoException).isNull()
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
        ) { logtoException, result ->
            assertThat(logtoException).isNull()
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
        ) { logtoException, result ->
            assertThat(logtoException).isNull()
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
        every { logtoConfigMock.endpoint } returns "https://logto.dev"

        mockkObject(Core)
        every { Core.fetchOidcConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val logtoClient = LogtoClient(logtoConfigMock, mockk())

        logtoClient.getOidcConfig { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result).isEqualTo(oidcConfigResponseMock)
        }
    }

    @Test
    fun `getOidcConfig success more than one time should only fetch once`() {
        every { logtoConfigMock.endpoint } returns "https://logto.dev"

        mockkObject(Core)
        every { Core.fetchOidcConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val logtoClient = LogtoClient(logtoConfigMock, mockk())

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
        logtoClient = LogtoClient(logtoConfigMock, mockk())
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
        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupIdToken(TEST_ID_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns false

        logtoClient.getIdTokenClaims { throwable, result ->
            assertThat(throwable)
                .hasMessageThat()
                .contains(LogtoException.Message.NOT_AUTHENTICATED.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getIdTokenClaims should fail if decodeIdToken failed`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupIdToken(TEST_ID_TOKEN)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        val invalidJwtExceptionMock: InvalidJwtException = mockk()

        mockkObject(TokenUtils)
        every { TokenUtils.decodeIdToken(any()) } throws invalidJwtExceptionMock

        logtoClient.getIdTokenClaims { logtoException, result ->
            assertThat(logtoException)
                .hasMessageThat()
                .contains(LogtoException.Message.UNABLE_TO_PARSE_ID_TOKEN_CLAIMS.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun fetchUserInfo() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())

        every { oidcConfigResponseMock.userinfoEndpoint } returns TEST_USERINFO_ENDPOINT

        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }
        val accessTokenMock: AccessToken = mockk()
        every { accessTokenMock.token } returns TEST_ACCESS_TOKEN
        every { logtoClient.getAccessToken(any(), any(), any()) } answers {
            lastArg<Completion<AccessToken>>().onComplete(null, accessTokenMock)
        }

        val userInfoResponseMock: UserInfoResponse = mockk()

        mockkObject(Core)
        every { Core.fetchUserInfo(any(), any(), any()) } answers {
            lastArg<HttpCompletion<UserInfoResponse>>().onComplete(null, userInfoResponseMock)
        }

        logtoClient.fetchUserInfo { logtoException, result ->
            assertThat(logtoException).isNull()
            assertThat(result).isEqualTo(userInfoResponseMock)
        }
    }

    private fun setupRefreshTokenTestEnv() {
        every { logtoConfigMock.scopes } returns listOf(TEST_SCOPE_1, TEST_SCOPE_2)
        every { logtoConfigMock.clientId } returns TEST_CLIENT_ID

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)
        every { logtoClient.isAuthenticated } returns true

        every { oidcConfigResponseMock.tokenEndpoint } returns TEST_TOKEN_ENDPOINT
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
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
