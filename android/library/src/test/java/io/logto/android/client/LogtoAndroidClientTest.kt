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

    @Mock
    private lateinit var oidcConfigurationBlockMock: (oidcConfiguration: OidcConfiguration) -> Unit

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
        val logtoConfigMock: LogtoConfig = mock()
        val logtoServiceMock: LogtoService = mock()
        val oidcConfigurationMock: OidcConfiguration = mock()
        val logtoAndroidClientSpy = spy(LogtoAndroidClient(logtoConfigMock, logtoServiceMock))
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()

        logtoAndroidClientSpy.getOidcConfiguration(oidcConfigurationBlockMock)

        verify(oidcConfigurationBlockMock).invoke(eq(oidcConfigurationMock))
    }

    @Test
    fun getOidcConfigurationMoreThenOnceShouldJustFetchOnce(): Unit = runBlocking {
        val logtoConfigMock: LogtoConfig = mock()
        val logtoServiceMock: LogtoService = mock()
        val logtoAndroidClientSpy = spy(LogtoAndroidClient(logtoConfigMock, logtoServiceMock))
        val oidcConfigurationMock: OidcConfiguration = mock()
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).fetchOidcConfiguration()

        logtoAndroidClientSpy.getOidcConfiguration()
        logtoAndroidClientSpy.getOidcConfiguration()

        verify(logtoAndroidClientSpy, times(1)).fetchOidcConfiguration()
    }

    @Test
    fun getgetJsonWebKeySetMoreThenOnceShouldJustFetchOnce(): Unit = runBlocking {
        val logtoConfigMock: LogtoConfig = mock()
        val logtoServiceMock: LogtoService = mock()
        val logtoAndroidClientSpy = spy(LogtoAndroidClient(logtoConfigMock, logtoServiceMock))
        val jsonWebKeySetMock: JsonWebKeySet = mock()
        doReturn(jsonWebKeySetMock).`when`(logtoAndroidClientSpy).fetchJwks(anyOrNull())

        logtoAndroidClientSpy.getJsonWebKeySet()
        logtoAndroidClientSpy.getJsonWebKeySet()

        verify(logtoAndroidClientSpy, times(1)).fetchJwks(anyOrNull())
    }

    @Test
    fun grantTokenByAuthorizationCodeShouldCallBlock() = runBlocking {
        val logtoConfigMock: LogtoConfig = mock()
        val logtoServiceMock: LogtoService = mock()
        val logtoAndroidClientSpy = spy(LogtoAndroidClient(logtoConfigMock, logtoServiceMock))

        val oidcConfigurationMock: OidcConfiguration = mock()
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()

        val jwks: JsonWebKeySet = mock()
        doReturn(jwks).`when`(logtoAndroidClientSpy).getJsonWebKeySet()

        val tokenSetMock: TokenSet = mock()
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
        val logtoConfigMock: LogtoConfig = mock()
        val logtoServiceMock: LogtoService = mock()
        val logtoAndroidClientSpy = spy(LogtoAndroidClient(logtoConfigMock, logtoServiceMock))

        val oidcConfigurationMock: OidcConfiguration = mock()
        doReturn(oidcConfigurationMock).`when`(logtoAndroidClientSpy).getOidcConfiguration()

        val jwks: JsonWebKeySet = mock()
        doReturn(jwks).`when`(logtoAndroidClientSpy).getJsonWebKeySet()

        val tokenSetMock: TokenSet = mock()
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
