package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.storage.TokenSetStorage
import io.logto.client.config.LogtoConfig
import io.logto.client.exception.LogtoException
import io.logto.client.model.TokenSet
import org.jose4j.jwt.JwtClaims

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

    fun getAccessToken(block: (accessToken: String) -> Unit) = tokenSet?.let {
        if (!it.isExpired()) {
            block(it.accessToken)
            return@let
        }

        refreshTokenSet { newTokenSet ->
            block(newTokenSet.accessToken)
        }
    } ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)

    fun getIdTokenClaims(): JwtClaims {
        return tokenSet?.getIdTokenClaims()
            ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)
    }

    fun signInWithBrowser(
        context: Context,
        onComplete: (tokenSet: TokenSet) -> Unit,
    ) = AuthManager.start(
        context,
        BrowserSignInFlow(
            logtoAndroidClient,
        ) { exception, tokenSet ->
            if (exception != null) {
                throw exception
            }
            if (tokenSet == null) {
                throw LogtoException(LogtoException.UNKNOWN_ERROR)
            }
            updateTokenSet(tokenSet)
            AuthManager.reset()
            onComplete(tokenSet)
        }
    )

    fun signOutWithBrowser(
        context: Context,
    ) = tokenSet?.let {
        AuthManager.start(
            context,
            BrowserSignOutFlow(
                idToken = it.idToken,
                logtoAndroidClient = logtoAndroidClient,
            ) { exception ->
                updateTokenSet(null)
                AuthManager.reset()
                if (exception != null) {
                    throw exception
                }
            }
        )
    } ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)

    fun refreshTokenSet(
        block: (tokenSet: TokenSet) -> Unit,
    ) = tokenSet?.let {
        val refreshToken = it.refreshToken
            ?: throw LogtoException(LogtoException.REFRESH_TOKEN_IS_NOT_SUPPORTED)

        logtoAndroidClient.grantTokenByRefreshTokenAsync(refreshToken) { updatedTokenSet ->
            updateTokenSet(updatedTokenSet)
            block(updatedTokenSet)
        }
    } ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)

    private fun updateTokenSet(updatedTokenSet: TokenSet?) {
        tokenSet = updatedTokenSet
        tokenSetStorage?.tokenSet = updatedTokenSet
    }
}
