package io.logto.sdk.core

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.type.UserInfoResponse
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class CoreFetchTest {
    companion object {
        private const val TEST_AUTHORIZATION_ENDPOINT = "https://logto.dev/oidc/auth"
        private const val TEST_TOKEN_ENDPOINT = "https://logto.dev/oidc/token"
        private const val TEST_END_SESSION_ENDPOINT = "https://logto.dev/oidc/session/end"
        private const val TEST_USERINFO_ENDPOINT = "https://logto.dev/oidc/me"
        private const val TEST_REVOCATION_ENDPOINT = "https://logto.dev/oidc/token/revocation"
        private const val TEST_JWKS_URI = "https://logto.dev/oidc/jwks"
        private const val TEST_ISSUER = "http://localhost:443/oidc"
    }
    private lateinit var mockWebServer: MockWebServer
    private val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                "/oidc_config:good" -> MockResponse().apply {
                    setResponseCode(200)
                    setBody(
                        """
                        {
                            "authorization_endpoint": "$TEST_AUTHORIZATION_ENDPOINT",
                            "token_endpoint": "$TEST_TOKEN_ENDPOINT",
                            "end_session_endpoint": "$TEST_END_SESSION_ENDPOINT",
                            "userinfo_endpoint": "$TEST_USERINFO_ENDPOINT",
                            "revocation_endpoint": "$TEST_REVOCATION_ENDPOINT",
                            "jwks_uri": "$TEST_JWKS_URI",
                            "issuer": "$TEST_ISSUER"
                        }
                        """.trimIndent()
                    )
                }
                "/token:good" -> MockResponse().apply {
                    setResponseCode(200)
                    setBody(
                        """
                        {
                            "access_token": "123",
                            "refresh_token": "456",
                            "id_token": "789",
                            "token_type": "jwt",
                            "scope": "",
                            "expires_in": 123
                        }
                        """.trimIndent()
                    )
                }
                "/user:good" -> MockResponse().apply {
                    setResponseCode(200)
                    setBody(
                        """
                        {
                            "sub": "foo"
                        }
                        """.trimIndent()
                    )
                }
                "/revoke:good" -> MockResponse().setResponseCode(200)
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun fetchOidcConfig() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: OidcConfigResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchOidcConfig(
            "${mockWebServer.url("/oidc_config:good")}"
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        val expectedResponse = OidcConfigResponse(
            authorizationEndpoint = TEST_AUTHORIZATION_ENDPOINT,
            tokenEndpoint = TEST_TOKEN_ENDPOINT,
            endSessionEndpoint = TEST_END_SESSION_ENDPOINT,
            userinfoEndpoint = TEST_USERINFO_ENDPOINT,
            revocationEndpoint = TEST_REVOCATION_ENDPOINT,
            jwksUri = TEST_JWKS_URI,
            issuer = TEST_ISSUER
        )

        assertThat(throwableReceiver).isNull()
        assertThat(responseReceiver).isEqualTo(expectedResponse)
    }

    @Test
    fun fetchOidcConfigShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: OidcConfigResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchOidcConfig(
            "${mockWebServer.url("/oidc_config:bad")}"
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(responseReceiver).isNull()
    }

    @Test
    fun fetchTokenByAuthorizationCode() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: CodeTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByAuthorizationCode(
            tokenEndpoint = "${mockWebServer.url("/token:good")}",
            clientId = "clientId",
            redirectUri = "https://logto.dev/callback",
            codeVerifier = "codeVerifier",
            code = "code",
            resource = null
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(responseReceiver).isNotNull()
    }

    @Test
    fun fetchTokenByAuthorizationCodeShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: CodeTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByAuthorizationCode(
            tokenEndpoint = "${mockWebServer.url("/token:bad")}",
            clientId = "clientId",
            redirectUri = "https://logto.dev/callback",
            codeVerifier = "codeVerifier",
            code = "code",
            resource = null
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(responseReceiver).isNull()
    }

    @Test
    fun fetchTokenByRefreshToken() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: RefreshTokenTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByRefreshToken(
            tokenEndpoint = "${mockWebServer.url("/token:good")}",
            clientId = "clientId",
            refreshToken = "refreshToken",
            resource = null,
            scopes = null
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(responseReceiver).isNotNull()
    }

    @Test
    fun fetchTokenByRefreshTokenShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: RefreshTokenTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByRefreshToken(
            tokenEndpoint = "${mockWebServer.url("/token:bad")}",
            clientId = "clientId",
            refreshToken = "refreshToken",
            resource = null,
            scopes = null
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(responseReceiver).isNull()
    }

    @Test
    fun fetchUserInfo() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: UserInfoResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchUserInfo(
            userInfoEndpoint = "${mockWebServer.url("/user:good")}",
            accessToken = "accessToken"
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(responseReceiver).isNotNull()
    }

    @Test
    fun fetchUserInfoShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var responseReceiver: UserInfoResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchUserInfo(
            userInfoEndpoint = "${mockWebServer.url("/user:bad")}",
            accessToken = "accessToken"
        ) { throwable, response ->
            throwableReceiver = throwable
            responseReceiver = response
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(responseReceiver).isNull()
    }

    @Test
    fun revoke() {
        var throwableReceiver: Throwable? = null

        val countDownLatch = CountDownLatch(1)
        Core.revoke(
            revocationEndpoint = "${mockWebServer.url("/revoke:good")}",
            clientId = "clientId",
            token = "refreshToken"
        ) { throwable ->
            throwableReceiver = throwable
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
    }

    @Test
    fun revokeShouldFailWithUnsuccessfulResponse() {
        var throwableReceiver: Throwable? = null

        val countDownLatch = CountDownLatch(1)
        Core.revoke(
            revocationEndpoint = "${mockWebServer.url("/revoke:bad")}",
            clientId = "clientId",
            token = "refreshToken"
        ) { throwable ->
            throwableReceiver = throwable
            countDownLatch.countDown()
        }
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
    }
}
