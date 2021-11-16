package io.logto.android.client

import com.google.common.truth.Truth.assertThat
import io.logto.android.callback.OidcConfigurationCallback
import io.logto.android.callback.TokenSetCallback
import io.logto.client.config.LogtoConfig
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.jose4j.jwk.JsonWebKeySet
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LogtoAndroidClientTest {

    private val oidcConfigurationMock: OidcConfiguration = mock()

    private val jsonWebKeySetMock: JsonWebKeySet = mock()

    private val tokenSetMock: TokenSet = mock()

    private val logtoConfigMock: LogtoConfig = mock()

    private val logtoAndroidClientSpy = spy(LogtoAndroidClient(logtoConfigMock))

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun getOidcConfigurationAsyncShouldCallBlock() = runBlockingTest {
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        val oidcConfigurationCallbackMock: OidcConfigurationCallback = mock()

        logtoAndroidClientSpy.getOidcConfigurationAsync(oidcConfigurationCallbackMock)
            .invokeOnCompletion { throwable ->
                assertThat(throwable).isNull()
                verify(oidcConfigurationCallbackMock).invoke(eq(null), eq(oidcConfigurationMock))
            }
    }

    @Test
    fun getOidcConfigurationMoreThenOnceShouldJustFetchOnce() {
        runBlocking {
            launch {
                doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).fetchOidcConfiguration()

                logtoAndroidClientSpy.getOidcConfiguration()
                logtoAndroidClientSpy.getOidcConfiguration()

                verify(logtoAndroidClientSpy, times(1)).fetchOidcConfiguration()
            }
        }
    }

    @Test
    fun getJsonWebKeySetMoreThenOnceShouldJustFetchOnce() {
        runBlocking {
            launch {
                `when`(oidcConfigurationMock.jwksUri).thenReturn("jwksUri")
                doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).fetchOidcConfiguration()
                doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).fetchJwks(anyOrNull())

                logtoAndroidClientSpy.getJsonWebKeySet()
                logtoAndroidClientSpy.getJsonWebKeySet()

                verify(logtoAndroidClientSpy, times(1)).fetchJwks(anyOrNull())
            }
        }
    }

    @Test
    fun grantTokenByAuthorizationCodeAsyncShouldCallBlock() = runBlockingTest {
        `when`(logtoConfigMock.clientId).thenReturn("clientId")
        `when`(oidcConfigurationMock.tokenEndpoint).thenReturn("tokenEndpoint")
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).getJsonWebKeySet()
        doNothing().`when`(tokenSetMock).validateIdToken(anyOrNull(), anyOrNull())
        doReturn(tokenSetMock)
            .`when`(logtoAndroidClientSpy)
            .grantTokenByAuthorizationCode(anyString(), anyString(), anyString())
        val authorizationCode = UUID.randomUUID().toString()
        val codeVerifier = UUID.randomUUID().toString()
        val tokenSetCallbackMock: TokenSetCallback = mock()

        logtoAndroidClientSpy.grantTokenByAuthorizationCodeAsync(
            authorizationCode,
            codeVerifier,
            tokenSetCallbackMock
        ).invokeOnCompletion { throwable ->
            assertThat(throwable).isNull()
            verify(tokenSetCallbackMock).invoke(eq(null), eq(tokenSetMock))
        }
    }

    @Test
    fun grantTokenByRefreshTokenAsyncShouldCallBlock() = runBlockingTest {
        `when`(logtoConfigMock.clientId).thenReturn("clientId")
        `when`(oidcConfigurationMock.tokenEndpoint).thenReturn("tokenEndpoint")
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).getJsonWebKeySet()
        doNothing().`when`(tokenSetMock).validateIdToken(anyOrNull(), anyOrNull())
        doReturn(tokenSetMock).`when`(logtoAndroidClientSpy).grantTokenByRefreshToken(anyOrNull(), anyOrNull())
        val dummyRefreshToken = UUID.randomUUID().toString()
        val tokenSetCallbackMock: TokenSetCallback = mock()

        logtoAndroidClientSpy.grantTokenByRefreshTokenAsync(
            dummyRefreshToken,
            tokenSetCallbackMock,
        ).invokeOnCompletion { throwable ->
            assertThat(throwable).isNull()
            verify(tokenSetCallbackMock).invoke(eq(null), eq(tokenSetMock))
        }
    }
}
