package io.logto.android.client

import io.logto.client.config.LogtoConfig
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import io.logto.client.service.LogtoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.jose4j.jwk.JsonWebKeySet
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
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

    private val logtoAndroidClientSpy = spy(LogtoAndroidClient(mock(), mock()))

    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun getOidcConfigurationShouldCallBlock() = runBlocking {
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        val oidcConfigurationBlockMock: (oidcConfiguration: OidcConfiguration) -> Unit = mock()

        logtoAndroidClientSpy.getOidcConfiguration(oidcConfigurationBlockMock)

        verify(oidcConfigurationBlockMock).invoke(eq(oidcConfigurationMock))
    }

    @Test
    fun getOidcConfigurationMoreThenOnceShouldJustFetchOnce(): Unit = runBlocking {
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).fetchOidcConfiguration()

        logtoAndroidClientSpy.getOidcConfiguration()
        logtoAndroidClientSpy.getOidcConfiguration()

        verify(logtoAndroidClientSpy, times(1)).fetchOidcConfiguration()
    }

    @Test
    fun getgetJsonWebKeySetMoreThenOnceShouldJustFetchOnce(): Unit = runBlocking {
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).fetchJwks(anyOrNull())

        logtoAndroidClientSpy.getJsonWebKeySet()
        logtoAndroidClientSpy.getJsonWebKeySet()

        verify(logtoAndroidClientSpy, times(1)).fetchJwks(anyOrNull())
    }

    @Test
    fun grantTokenByAuthorizationCodeShouldCallBlock() = runBlocking {
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).getJsonWebKeySet()
        doNothing().`when`(tokenSetMock).validateIdToken(anyOrNull(), anyOrNull())
        doReturn(tokenSetMock)
            .`when`(logtoAndroidClientSpy)
            .grantTokenByAuthorizationCode(anyOrNull(), anyString(), anyString())

        val authorizationCode = UUID.randomUUID().toString()
        val codeVerifier = UUID.randomUUID().toString()

        val tokenSetBlockMock: (tokenSet: TokenSet) -> Unit = mock()

        logtoAndroidClientSpy.grantTokenByAuthorizationCode(
            authorizationCode,
            codeVerifier,
            tokenSetBlockMock
        )

        verify(tokenSetBlockMock).invoke(eq(tokenSetMock))
    }

    @Test
    fun grantTokenByRefreshTokenShouldCallBlock() = runBlocking {
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).getJsonWebKeySet()
        doNothing().`when`(tokenSetMock).validateIdToken(anyOrNull(), anyOrNull())
        doReturn(tokenSetMock)
            .`when`(logtoAndroidClientSpy)
            .grantTokenByRefreshToken(anyOrNull(), anyString())
        val refreshToken = UUID.randomUUID().toString()
        val tokenSetBlockMock: (tokenSet: TokenSet) -> Unit = mock()

        logtoAndroidClientSpy.grantTokenByRefreshToken(
            refreshToken,
            tokenSetBlockMock
        )

        verify(tokenSetBlockMock).invoke(eq(tokenSetMock))
    }
}
