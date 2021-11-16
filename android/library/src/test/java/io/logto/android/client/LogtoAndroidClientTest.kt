package io.logto.android.client

import com.google.common.truth.Truth.assertThat
import io.logto.android.callback.HandleOidcConfigurationCallback
import io.logto.android.callback.HandleTokenSetCallback
import io.logto.client.config.LogtoConfig
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
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

    private val logtoAndroidClient = LogtoAndroidClient(logtoConfigMock)

    private val testCoroutineScope = TestCoroutineScope()

    private lateinit var logtoAndroidClientSpy: LogtoAndroidClient

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        logtoAndroidClient.setCoroutineScope(testCoroutineScope)
        logtoAndroidClientSpy = spy(logtoAndroidClient)
    }

    @Test
    fun getOidcConfigurationAsyncShouldCallBlock() {
        runBlockingTest {
            doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
            val handleOidcConfigurationCallbackMock: HandleOidcConfigurationCallback = mock()

            logtoAndroidClientSpy.getOidcConfigurationAsync(handleOidcConfigurationCallbackMock)
                .invokeOnCompletion { throwable ->
                    assertThat(throwable).isNull()
                    verify(handleOidcConfigurationCallbackMock)
                        .invoke(eq(null), eq(oidcConfigurationMock))
                }
        }
    }

    @Test
    fun getOidcConfigurationMoreThenOnceShouldJustFetchOnce() {
        runBlockingTest {
            doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy)
                .fetchOidcConfiguration()

            logtoAndroidClientSpy.getOidcConfiguration()
            logtoAndroidClientSpy.getOidcConfiguration()

            verify(logtoAndroidClientSpy, times(1)).fetchOidcConfiguration()
        }
    }

    @Test
    fun getJsonWebKeySetMoreThenOnceShouldJustFetchOnce() {
        runBlockingTest {
            `when`(oidcConfigurationMock.jwksUri).thenReturn("jwksUri")
            doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy)
                .fetchOidcConfiguration()
            doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).fetchJwks(anyOrNull())

            logtoAndroidClientSpy.getJsonWebKeySet()
            logtoAndroidClientSpy.getJsonWebKeySet()

            verify(logtoAndroidClientSpy, times(1)).fetchJwks(anyOrNull())
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
        val handleTokenSetCallbackMock: HandleTokenSetCallback = mock()

        logtoAndroidClientSpy.grantTokenByAuthorizationCodeAsync(
            authorizationCode,
            codeVerifier,
            handleTokenSetCallbackMock
        ).invokeOnCompletion { throwable ->
            assertThat(throwable).isNull()
            verify(handleTokenSetCallbackMock).invoke(eq(null), eq(tokenSetMock))
        }
    }

    @Test
    fun grantTokenByRefreshTokenAsyncShouldCallBlock() = runBlockingTest {
        `when`(logtoConfigMock.clientId).thenReturn("clientId")
        `when`(oidcConfigurationMock.tokenEndpoint).thenReturn("tokenEndpoint")
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).getJsonWebKeySet()
        doNothing().`when`(tokenSetMock).validateIdToken(anyOrNull(), anyOrNull())
        doReturn(tokenSetMock).`when`(logtoAndroidClientSpy)
            .grantTokenByRefreshToken(anyOrNull(), anyOrNull())
        val dummyRefreshToken = UUID.randomUUID().toString()
        val handleTokenSetCallbackMock: HandleTokenSetCallback = mock()

        logtoAndroidClientSpy.grantTokenByRefreshTokenAsync(
            dummyRefreshToken,
            handleTokenSetCallbackMock,
        ).invokeOnCompletion { throwable ->
            assertThat(throwable).isNull()
            verify(handleTokenSetCallbackMock).invoke(eq(null), eq(tokenSetMock))
        }
    }
}
