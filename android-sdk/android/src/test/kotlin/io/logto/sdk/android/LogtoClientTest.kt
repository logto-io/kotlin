package io.logto.sdk.android

import android.webkit.CookieManager
import com.google.common.truth.Truth.assertThat
import io.logto.sdk.android.auth.logto.LogtoAuthSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.util.LogtoUtils
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.http.HttpEmptyCompletion
import io.logto.sdk.core.type.IdTokenClaims
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.util.TokenUtils
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.verify
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwt.consumer.InvalidJwtException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogtoClientTest {
    private val oidcConfigResponseMock: OidcConfigResponse = mockk()
    private val jwksMock: JsonWebKeySet = mockk()
    private val logtoConfigMock: LogtoConfig = mockk()
    private lateinit var logtoClient: LogtoClient

    private val timeBias = 10L

    companion object {
        private const val TEST_SCOPE = "scope"

        private const val TEST_RESOURCE_1 = "resource_1"
        private const val TEST_RESOURCE_2 = "resource_2"
        private const val TEST_RESOURCE_3 = "resource_3"

        private const val TEST_APP_ID = "app_id"
        private const val TEST_REFRESH_TOKEN = "refreshToken"
        private const val TEST_TOKEN_ENDPOINT = "tokenEndpoint"
        private const val TEST_REVOCATION_ENDPOINT = "endSessionEndpoint"
        private const val TEST_ISSUER = "issuer"
        private const val TEST_ACCESS_TOKEN = "accessToken"
        private const val TEST_ID_TOKEN = "idToken"
        private const val TEST_EXPIRE_IN = 60L
        private const val TEST_JWKS_JSON = """
            {
                "keys": [
                    {
                        "kty": "RSA",
                        "use": "sig",
                        "kid": "Cskl6H4FGsi-q4BEOOPP9belshDaGf7wEubUNJBYpBk",
                        "e": "AQAB",
                        "n": "pB5nO7qovnRQrSQoVmdh0g6TGtMMjc1eS0rexzcuVIgtD-7-84DHt9FaiS8UVr2Tjdp_U4Jr-mJJNbYhxae2FjNkpWf_ETND8hEYTSCZTJCkX0asnzb-xZgt2_xNiOAUzmXEaSHO215Y-WYL2LydLjoMrK70FfoFC4jnsgnnKlf1fQW2llCpG-b19w-aHU5m8fPOWKz5n27jEYNbEqHK-wsGavt7eyhVfEVPNbVl5j_n8o-VfnQT-LyO4Fg6U0XwHz1yXrT7NUMO_qdfwv1QbM0EPyWkxLoSColRZVibPmMpkc9RcOJ2crP5u602W8UOYvbtcBCaXVbzp5iriBAVxRq3tsrnTpHr-1FV5jtwU1aLMucIkOM3iJGSLoLizgwEIAnmLh1u_-lxFeSEWDX3RIE3kZOWdZoRBKcxCYPV4X7Mkca8UNW42FTeUG8f9bq43_FgZvWnnFBYpzTuHTnLlkw1a3GmjRy02_tqhV7xp5rM65Jc8HZEW81L3JKLp87ySqjKWfBkmI0ebzEPZVwV69ggI6eBVzGK1nViHsBWgDAomBGPVUqfZmACIcdy7hOp-40mDa6RscqBFtpd3RPb6lGyf2yDCH-4AY6ZRQUX10TdtW2NQon8-SBNgye4x5ZiUS7EXFxvIaTEZ_MZryS3yo5_xWtYAZLCJrDqEZLY2mE"
                    }
                ]
            }
        """
    }

    @Before
    fun setup() {
        every { logtoConfigMock.usingPersistStorage } returns false
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `signIn should complete with exception if get oidc config failed`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())
        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            lastArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(
                LogtoException(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG),
                null,
            )
        }

        logtoClient.signIn(mockk(), "dummyRedirectUri") { logtoException ->
            assertThat(logtoException)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG.name)
        }
    }

    @Test
    fun `signIn should start a logto auth session`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())
        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            lastArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(
                null,
                oidcConfigResponseMock,
            )
        }

        mockkConstructor(LogtoAuthSession::class)
        every {
            anyConstructed<LogtoAuthSession>().start()
        } just Runs

        logtoClient.signIn(mockk(), "dummyRedirectUri", mockk())

        verify {
            anyConstructed<LogtoAuthSession>().start()
        }
    }

    @Test
    fun `signOut should clear all relative data`() {
        every { logtoConfigMock.appId } returns TEST_APP_ID

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)

        logtoClient.setupIdToken("dummyIdToken")
        logtoClient.setupRefreshToken("dummyRefreshToken")

        every { oidcConfigResponseMock.revocationEndpoint } returns TEST_REVOCATION_ENDPOINT
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val cookieManagerInstance = CookieManager.getInstance()
        mockkObject(cookieManagerInstance)
        every { cookieManagerInstance.removeAllCookies(any()) } just Runs
        every { cookieManagerInstance.flush() } just Runs

        mockkObject(Core)
        every { Core.revoke(any(), any(), any(), any()) } just Runs

        logtoClient.signOut(mockk())

        verify {
            Core.revoke(any(), any(), any(), any())
        }

        assertThat(logtoClient.isAuthenticated).isFalse()
    }

    @Test
    fun `signOut should complete with exception if not authenticated`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)

        every { logtoClient.isAuthenticated } returns false

        logtoClient.signOut {
            assertThat(it)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.NOT_AUTHENTICATED.name)
        }
    }

    @Test
    fun `signOut should complete with exception if get oidc config failed`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)

        logtoClient.setupRefreshToken("dummyRefreshToken")
        logtoClient.setupIdToken("dummyIdToken")

        every { logtoClient.getOidcConfig(any()) } answers {
            lastArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(
                LogtoException(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG),
                null,
            )
        }

        logtoClient.signOut {
            assertThat(it)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG.name)
        }

        assertThat(logtoClient.isAuthenticated).isFalse()
    }

    @Test
    fun `signOutWithBrowser should complete with exception if revoke failed`() {
        every { logtoConfigMock.appId } returns TEST_APP_ID

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)

        logtoClient.setupRefreshToken("dummyRefreshToken")
        logtoClient.setupIdToken("dummyIdToken")

        every { oidcConfigResponseMock.revocationEndpoint } returns TEST_REVOCATION_ENDPOINT
        every { logtoClient.getOidcConfig(any()) } answers {
            lastArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(
                null,
                oidcConfigResponseMock,
            )
        }

        mockkObject(Core)
        every { Core.revoke(any(), any(), any(), any()) } answers {
            lastArg<HttpEmptyCompletion>().onComplete(LogtoException(LogtoException.Type.UNABLE_TO_REVOKE_TOKEN))
        }

        logtoClient.signOut {
            assertThat(it)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.UNABLE_TO_REVOKE_TOKEN.name)
        }

        assertThat(logtoClient.isAuthenticated).isFalse()
    }

    @Test
    fun `getAccessToken should fail without being authenticated`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns false

        logtoClient.getAccessToken { logtoException, result ->
            assertThat(logtoException).hasMessageThat().contains(LogtoException.Type.NOT_AUTHENTICATED.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should fail without refreshToken`() {

        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupRefreshToken(null)

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken { logtoException, result ->
            assertThat(logtoException).hasMessageThat().contains(LogtoException.Type.NOT_AUTHENTICATED.name)
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
        ) { logtoException, result ->
            assertThat(logtoException)
                .hasMessageThat()
                .contains(LogtoException.Type.UNGRANTED_RESOURCE_FOUND.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getAccessToken should return valid accessToken which is already exist`() {

        logtoClient = LogtoClient(logtoConfigMock, mockk())
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)

        val testTokenKey = logtoClient.buildAccessTokenKey(null, null)
        val testAccessToken: AccessToken = mockk()
        every { testAccessToken.expiresAt } returns LogtoUtils.nowRoundToSec() + timeBias

        logtoClient.setupAccessTokenMap(mapOf(testTokenKey to testAccessToken))

        mockkObject(logtoClient)
        every { logtoClient.isAuthenticated } returns true

        logtoClient.getAccessToken(
            null,
        ) { logtoException, result ->
            assertThat(logtoException).isNull()
            assertThat(result).isEqualTo(testAccessToken)
        }
    }

    @Test
    fun `getAccessToken should refresh token when existing accessToken is expired`() {
        setupRefreshTokenTestEnv()

        val expiredAccessTokenKey = logtoClient.buildAccessTokenKey(null, null)
        val expiredAccessToken: AccessToken = mockk()
        every { expiredAccessToken.expiresAt } returns LogtoUtils.nowRoundToSec() - timeBias
        logtoClient.setupAccessTokenMap(mapOf(expiredAccessTokenKey to expiredAccessToken))

        logtoClient.getAccessToken(
            null,
        ) { logtoException, result ->
            assertThat(logtoException).isNull()
            assertThat(result).isNotNull()
            requireNotNull(result).apply {
                assertThat(token).isEqualTo(TEST_ACCESS_TOKEN)
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
        ) { logtoException, result ->
            assertThat(logtoException).isNull()
            assertThat(result).isNotNull()
            requireNotNull(result).apply {
                assertThat(token).isEqualTo(TEST_ACCESS_TOKEN)
            }
        }

        verify(exactly = 1) {
            Core.fetchTokenByRefreshToken(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `getOidcConfig should complete with oidc config if fetchOidcConfig success`() {
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
    fun `getOidcConfig success more than once should only fetch once`() {
        every { logtoConfigMock.endpoint } returns "https://logto.dev"

        mockkObject(Core)
        every { Core.fetchOidcConfig(any(), any()) } answers {
            secondArg<HttpCompletion<OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        logtoClient = LogtoClient(logtoConfigMock, mockk())

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
    fun `getIdTokenClaims should complete with token claims`() {
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
                .contains(LogtoException.Type.NOT_AUTHENTICATED.name)
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
                .contains(LogtoException.Type.UNABLE_TO_PARSE_ID_TOKEN_CLAIMS.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getJwks should complete with jwks`() {
        every { oidcConfigResponseMock.jwksUri } returns "https://logto.dev/oidc/jwks"

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        mockkObject(Core)
        every { Core.fetchJwksJson(any(), any()) } answers {
            secondArg<HttpCompletion<String>>().onComplete(null, TEST_JWKS_JSON)
        }

        val expectedJwks = JsonWebKeySet(TEST_JWKS_JSON)

        logtoClient.getJwks { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result?.toJson()).isEqualTo(expectedJwks.toJson())
        }
    }

    @Test
    fun `getJwks success more than once should only fetch once`() {
        every { oidcConfigResponseMock.jwksUri } returns "https://logto.dev/oidc/jwks"

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        mockkObject(Core)
        every { Core.fetchJwksJson(any(), any()) } answers {
            secondArg<HttpCompletion<String>>().onComplete(null, TEST_JWKS_JSON)
        }

        val expectedJwks = JsonWebKeySet(TEST_JWKS_JSON)

        logtoClient.getJwks { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result?.toJson()).isEqualTo(expectedJwks.toJson())
        }

        logtoClient.getJwks { throwable, result ->
            assertThat(throwable).isNull()
            assertThat(result?.toJson()).isEqualTo(expectedJwks.toJson())
        }

        verify(exactly = 1) {
            Core.fetchJwksJson(any(), any())
        }
    }

    @Test
    fun `getJwks should complete with exception if get oidc config failed`() {
        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(
                LogtoException(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG),
                null,
            )
        }

        logtoClient.getJwks { throwable, result ->
            assertThat(throwable)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getJwks should complete with exception if fetchJwksJson failed`() {
        every { oidcConfigResponseMock.jwksUri } returns "https://logto.dev/oidc/jwks"

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        mockkObject(Core)
        every { Core.fetchJwksJson(any(), any()) } answers {
            secondArg<HttpCompletion<String>>().onComplete(
                LogtoException(LogtoException.Type.UNABLE_TO_FETCH_JWKS_JSON),
                null,
            )
        }

        logtoClient.getJwks { throwable, result ->
            assertThat(throwable)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.UNABLE_TO_FETCH_JWKS_JSON.name)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `getJwks should complete with exception if got an invalid jwks JSON from fetchJwksJson`() {
        every { oidcConfigResponseMock.jwksUri } returns "https://logto.dev/oidc/jwks"

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        val invalidJwksJson = "invalidJwksJson"

        mockkObject(Core)
        every { Core.fetchJwksJson(any(), any()) } answers {
            secondArg<HttpCompletion<String>>().onComplete(null, invalidJwksJson)
        }

        logtoClient.getJwks { throwable, result ->
            assertThat(throwable)
                .hasMessageThat()
                .isEqualTo(LogtoException.Type.UNABLE_TO_PARSE_JWKS.name)
            assertThat(result).isNull()
        }
    }

    private fun setupRefreshTokenTestEnv() {
        every { logtoConfigMock.appId } returns TEST_APP_ID

        logtoClient = LogtoClient(logtoConfigMock, mockk())

        mockkObject(logtoClient)
        logtoClient.setupRefreshToken(TEST_REFRESH_TOKEN)
        every { logtoClient.isAuthenticated } returns true

        every { oidcConfigResponseMock.tokenEndpoint } returns TEST_TOKEN_ENDPOINT
        every { oidcConfigResponseMock.issuer } returns TEST_ISSUER
        every { logtoClient.getOidcConfig(any()) } answers {
            firstArg<Completion<LogtoException, OidcConfigResponse>>().onComplete(null, oidcConfigResponseMock)
        }

        every { logtoClient.getJwks((any())) } answers {
            firstArg<Completion<LogtoException, JsonWebKeySet>>().onComplete(null, jwksMock)
        }

        val refreshTokenTokenResponseMock: RefreshTokenTokenResponse = mockk()
        every { refreshTokenTokenResponseMock.accessToken } returns TEST_ACCESS_TOKEN
        every { refreshTokenTokenResponseMock.scope } returns TEST_SCOPE
        every { refreshTokenTokenResponseMock.expiresIn } returns TEST_EXPIRE_IN
        every { refreshTokenTokenResponseMock.refreshToken } returns TEST_REFRESH_TOKEN
        every { refreshTokenTokenResponseMock.idToken } returns TEST_ID_TOKEN

        mockkObject(Core)
        every { Core.fetchTokenByRefreshToken(any(), any(), any(), any(), any(), any()) } answers {
            lastArg<HttpCompletion<RefreshTokenTokenResponse>>()
                .onComplete(null, refreshTokenTokenResponseMock)
        }

        mockkObject(TokenUtils)
        every { TokenUtils.verifyIdToken(any(), any(), any(), any()) } just Runs
    }
}
