package io.logto.android.client

import com.google.common.truth.Truth.assertThat
import io.logto.android.callback.HandleOidcConfigurationCallback
import io.logto.android.callback.HandleTokenSetCallback
import io.logto.client.config.LogtoConfig
import io.logto.client.exception.LogtoException
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.jose4j.jwk.JsonWebKeySet
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LogtoAndroidClientTest {

    @RelaxedMockK
    private lateinit var oidcConfigurationMock: OidcConfiguration

    @RelaxedMockK
    private lateinit var jsonWebKeySetMock: JsonWebKeySet

    @RelaxedMockK
    private lateinit var tokenSetMock: TokenSet

    @RelaxedMockK
    private lateinit var logtoConfigMock: LogtoConfig

    private lateinit var logtoAndroidClient: LogtoAndroidClient

    private lateinit var logtoAndroidClientSpy: LogtoAndroidClient

    private val testCoroutineScope = TestCoroutineScope()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        logtoAndroidClient = LogtoAndroidClient(logtoConfigMock)
        logtoAndroidClient.setCoroutineScope(testCoroutineScope)
        logtoAndroidClientSpy = spyk(logtoAndroidClient)
    }

    @After
    fun tearDown() {
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun getOidcConfigurationAsyncShouldCallBlock() = runBlockingTest {
        coEvery {
            logtoAndroidClientSpy.getOidcConfiguration()
        } returns oidcConfigurationMock
        val handleOidcConfigurationCallbackMock: HandleOidcConfigurationCallback =
            mockk(relaxed = true)

        logtoAndroidClientSpy.getOidcConfigurationAsync(handleOidcConfigurationCallbackMock)
            .invokeOnCompletion { throwable ->
                assertThat(throwable).isNull()

                val logtoExceptionCaptureList = mutableListOf<LogtoException?>()
                coVerify {
                    handleOidcConfigurationCallbackMock.invoke(
                        captureNullable(logtoExceptionCaptureList),
                        eq(oidcConfigurationMock)
                    )
                }
                assertThat(logtoExceptionCaptureList.last()).isNull()
            }
    }

    @Test
    fun grantTokenByAuthorizationCodeAsyncShouldCallBlock() = runBlockingTest {
        every { logtoConfigMock.clientId } returns "clientId"
        every { oidcConfigurationMock.tokenEndpoint } returns "tokenEndpoint"
        coEvery { logtoAndroidClientSpy.getOidcConfiguration() } returns oidcConfigurationMock
        coEvery { logtoAndroidClientSpy.getJsonWebKeySet() } returns jsonWebKeySetMock
        coEvery {
            logtoAndroidClientSpy.grantTokenByAuthorizationCode(any(), any(), any())
        } returns tokenSetMock
        val authorizationCode = UUID.randomUUID().toString()
        val codeVerifier = UUID.randomUUID().toString()
        val handleTokenSetCallbackMock: HandleTokenSetCallback = mockk(relaxed = true)

        logtoAndroidClientSpy.grantTokenByAuthorizationCodeAsync(
            authorizationCode,
            codeVerifier,
            handleTokenSetCallbackMock
        ).invokeOnCompletion { throwable ->
            assertThat(throwable).isNull()
            verify { handleTokenSetCallbackMock.invoke(isNull(), eq(tokenSetMock)) }
        }
    }

    @Test
    fun grantTokenByRefreshTokenAsyncShouldCallBlock() = runBlockingTest {
        every { logtoConfigMock.clientId } returns "clientId"
        every { oidcConfigurationMock.tokenEndpoint } returns "tokenEndpoint"
        coEvery { logtoAndroidClientSpy.getOidcConfiguration() } returns oidcConfigurationMock
        coEvery { logtoAndroidClientSpy.getJsonWebKeySet() } returns jsonWebKeySetMock
        coEvery {
            logtoAndroidClientSpy.grantTokenByRefreshToken(any(), any())
        } returns tokenSetMock
        val dummyRefreshToken = UUID.randomUUID().toString()
        val handleTokenSetCallbackMock: HandleTokenSetCallback = mockk(relaxed = true)

        logtoAndroidClientSpy.grantTokenByRefreshTokenAsync(
            dummyRefreshToken,
            handleTokenSetCallbackMock,
        ).invokeOnCompletion { throwable ->
            assertThat(throwable).isNull()
            verify { handleTokenSetCallbackMock.invoke(isNull(), eq(tokenSetMock)) }
        }
    }
}
