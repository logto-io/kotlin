package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
import io.logto.android.callback.HandleAccessTokenCallback
import io.logto.android.callback.HandleIdTokenClaimsCallback
import io.logto.android.callback.HandleLogtoExceptionCallback
import io.logto.android.callback.HandleTokenSetCallback
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.storage.TokenSetStorage
import io.logto.client.config.LogtoConfig
import io.logto.client.exception.LogtoException
import io.logto.client.model.TokenSet

class Logto(
    logtoConfig: LogtoConfig,
    application: Application,
    useStorage: Boolean = true,
) {
    private companion object {
        private const val STORAGE_SHAREDPREFERENCES_NAME_PREFIX = "io.logto.android"
    }

    private val logtoAndroidClient = LogtoAndroidClient(logtoConfig)

    private var tokenSet: TokenSet? = null

    private val tokenSetStorageSharedPreferencesName =
        "$STORAGE_SHAREDPREFERENCES_NAME_PREFIX::${logtoConfig.cacheKey}"

    private var tokenSetStorage: TokenSetStorage? = null

    init {
        if (useStorage) {
            val storage = TokenSetStorage(application, tokenSetStorageSharedPreferencesName)
            tokenSetStorage = storage
            tokenSet = storage.tokenSet
        }
    }

    val authenticated: Boolean
        get() = tokenSet != null

    fun getAccessToken(
        block: HandleAccessTokenCallback
    ) {
        if (tokenSet == null) {
            block(LogtoException(LogtoException.NOT_AUTHENTICATED), null)
            return
        }

        if (!tokenSet!!.isExpired()) {
            block(null, tokenSet!!.accessToken)
            return
        }

        refreshTokenSet { exception, newTokenSet ->
            block(exception, newTokenSet?.accessToken)
        }
    }

    fun getIdTokenClaims(block: HandleIdTokenClaimsCallback) {
        if (tokenSet == null) {
            block(LogtoException(LogtoException.NOT_AUTHENTICATED), null)
            return
        }
        try {
            val idTokenClaims = tokenSet!!.getIdTokenClaims()
            block(null, idTokenClaims)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    fun signInWithBrowser(
        context: Context,
        block: HandleTokenSetCallback,
    ) {
        val signInFlow = BrowserSignInFlow(
            logtoAndroidClient,
        ) { exception, tokenSet ->
            if (exception != null) {
                block(exception, null)
                return@BrowserSignInFlow
            }
            if (tokenSet == null) {
                block(LogtoException(LogtoException.UNKNOWN_ERROR), null)
                return@BrowserSignInFlow
            }
            updateTokenSet(tokenSet)
            AuthManager.reset()
            block(null, tokenSet)
        }

        AuthManager.start(context, signInFlow)
    }

    fun signOutWithBrowser(
        context: Context,
        block: HandleLogtoExceptionCallback? = null
    ) {
        if (tokenSet == null) {
            block?.invoke(LogtoException(LogtoException.NOT_AUTHENTICATED))
            return
        }

        val browserSignOutFlow = BrowserSignOutFlow(
            idToken = tokenSet!!.idToken,
            logtoAndroidClient = logtoAndroidClient,
        ) { exception ->
            updateTokenSet(null)
            AuthManager.reset()
            block?.invoke(exception)
        }

        AuthManager.start(context, browserSignOutFlow)
    }

    private fun refreshTokenSet(block: HandleTokenSetCallback) {
        if (tokenSet == null) {
            block(LogtoException(LogtoException.NOT_AUTHENTICATED), null)
            return
        }

        val refreshToken = tokenSet!!.refreshToken
        if (refreshToken == null) {
            block(LogtoException(LogtoException.REFRESH_TOKEN_IS_NOT_SUPPORTED), null)
            return
        }

        logtoAndroidClient.grantTokenByRefreshTokenAsync(refreshToken) { exception, updatedTokenSet ->
            updateTokenSet(updatedTokenSet)
            block(exception, updatedTokenSet)
        }
    }

    private fun updateTokenSet(updatedTokenSet: TokenSet?) {
        tokenSet = updatedTokenSet
        tokenSetStorage?.tokenSet = updatedTokenSet
    }
}
