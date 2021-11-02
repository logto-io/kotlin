package io.logto.android.auth.browser

import android.app.Activity
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.exception.LogtoException
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BrowserSignInFlowTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldStartSignIn() {
        val logtoConfig: LogtoConfig = mock()
        `when`(logtoConfig.redirectUri).thenReturn("redirectUri")

        val logtoApiClient: LogtoApiClient = mock()
        val oidcConfiguration: OidcConfiguration = mock()
        `when`(oidcConfiguration.authorizationEndpoint).thenReturn("authorizationEndpoint")

        doAnswer { invocationOnMock ->
            val block = invocationOnMock.arguments[0] as (OidcConfiguration) -> Unit
            block(oidcConfiguration)
            null
        }.`when`(logtoApiClient).discover(anyOrNull())

        val onComplete: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit = mock()
        val activity: Activity = spy(Robolectric.buildActivity(Activity::class.java).get())

        val browserSignInFlow = BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
            onComplete
        )

        browserSignInFlow.start(activity)
        verify(activity).startActivity(anyOrNull())
    }
}
