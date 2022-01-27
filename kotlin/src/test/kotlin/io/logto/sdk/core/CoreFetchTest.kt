package io.logto.sdk.core

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.http.HttpEmptyCompletion
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
    private lateinit var mockWebServer: MockWebServer
    private val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                "/oidc_config:good" -> MockResponse().apply {
                    setResponseCode(200)
                    setBody(
                        """
                        {
                            "authorization_endpoint": "https://logto.dev/oidc/auth",
                            "token_endpoint": "https://logto.dev/oidc/token",
                            "end_session_endpoint": "https://logto.dev/oidc/session/end",
                            "revocation_endpoint": "https://logto.dev/oidc/token/revocation",
                            "jwks_uri": "https://logto.dev/oidc/jwks",
                            "issuer": "http://localhost:443/oidc"
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
        var resultReceiver: OidcConfigResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchOidcConfig(
            "${mockWebServer.url("/oidc_config:good")}",
            object : HttpCompletion<OidcConfigResponse> {
                override fun onComplete(throwable: Throwable?, result: OidcConfigResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(resultReceiver).isNotNull()
    }

    @Test
    fun fetchOidcConfigShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: OidcConfigResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchOidcConfig(
            "${mockWebServer.url("/oidc_config:bad")}",
            object : HttpCompletion<OidcConfigResponse> {
                override fun onComplete(throwable: Throwable?, result: OidcConfigResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(resultReceiver).isNull()
    }

    @Test
    fun fetchTokenByAuthorizationCode() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: CodeTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByAuthorizationCode(
            tokenEndpoint = "${mockWebServer.url("/token:good")}",
            clientId = "clientId",
            redirectUri = "https://logto.dev/callback",
            codeVerifier = "codeVerifier",
            code = "code",
            resource = null,
            completion = object : HttpCompletion<CodeTokenResponse> {
                override fun onComplete(throwable: Throwable?, result: CodeTokenResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(resultReceiver).isNotNull()
    }

    @Test
    fun fetchTokenByAuthorizationCodeShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: CodeTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByAuthorizationCode(
            tokenEndpoint = "${mockWebServer.url("/token:bad")}",
            clientId = "clientId",
            redirectUri = "https://logto.dev/callback",
            codeVerifier = "codeVerifier",
            code = "code",
            resource = null,
            completion = object : HttpCompletion<CodeTokenResponse> {
                override fun onComplete(throwable: Throwable?, result: CodeTokenResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(resultReceiver).isNull()
    }

    @Test
    fun fetchTokenByRefreshToken() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: RefreshTokenTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByRefreshToken(
            tokenEndpoint = "${mockWebServer.url("/token:good")}",
            clientId = "clientId",
            refreshToken = "refreshToken",
            resource = null,
            scope = null,
            completion = object : HttpCompletion<RefreshTokenTokenResponse> {
                override fun onComplete(throwable: Throwable?, result: RefreshTokenTokenResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(resultReceiver).isNotNull()
    }

    @Test
    fun fetchTokenByRefreshTokenShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: RefreshTokenTokenResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchTokenByRefreshToken(
            tokenEndpoint = "${mockWebServer.url("/token:bad")}",
            clientId = "clientId",
            refreshToken = "refreshToken",
            resource = null,
            scope = null,
            completion = object : HttpCompletion<RefreshTokenTokenResponse> {
                override fun onComplete(throwable: Throwable?, result: RefreshTokenTokenResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(resultReceiver).isNull()
    }

    @Test
    fun fetchUserInfo() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: UserInfoResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchUserInfo(
            userInfoEndpoint = "${mockWebServer.url("/user:good")}",
            accessToken = "accessToken",
            completion = object : HttpCompletion<UserInfoResponse?> {
                override fun onComplete(throwable: Throwable?, result: UserInfoResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNull()
        assertThat(resultReceiver).isNotNull()
    }

    @Test
    fun fetchUserInfoShouldFailWithoutResponse() {
        var throwableReceiver: Throwable? = null
        var resultReceiver: UserInfoResponse? = null

        val countDownLatch = CountDownLatch(1)
        Core.fetchUserInfo(
            userInfoEndpoint = "${mockWebServer.url("/user:bad")}",
            accessToken = "accessToken",
            completion = object : HttpCompletion<UserInfoResponse?> {
                override fun onComplete(throwable: Throwable?, result: UserInfoResponse?) {
                    throwableReceiver = throwable
                    resultReceiver = result
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
        assertThat(resultReceiver).isNull()
    }

    @Test
    fun revoke() {
        var throwableReceiver: Throwable? = null

        val countDownLatch = CountDownLatch(1)
        Core.revoke(
            revocationEndpoint = "${mockWebServer.url("/revoke:good")}",
            clientId = "clientId",
            token = "refreshToken",
            completion = object : HttpEmptyCompletion {
                override fun onComplete(throwable: Throwable?) {
                    throwableReceiver = throwable
                    countDownLatch.countDown()
                }
            }
        )
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
            token = "refreshToken",
            completion = object : HttpEmptyCompletion {
                override fun onComplete(throwable: Throwable?) {
                    throwableReceiver = throwable
                    countDownLatch.countDown()
                }
            }
        )
        countDownLatch.await()

        assertThat(throwableReceiver).isNotNull()
    }
}
