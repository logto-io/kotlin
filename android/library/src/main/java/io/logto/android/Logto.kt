package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.api.LogtoService
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.exception.LogtoException
import io.logto.android.model.TokenSet
import io.logto.android.storage.TokenSetStorage
import io.logto.android.utils.Utils

class Logto(
    private val logtoConfig: LogtoConfig,
    application: Application,
    useStorage: Boolean = true,
) {
    fun isAuthenticated(): Boolean = tokenSet != null

    fun getAccessToken(block: (accessToken: String) -> Unit) {
        val cachedTokenSet = tokenSet ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)
        if (Utils.nowRoundToSec() < accessTokenExpiresAt) {
            block(cachedTokenSet.accessToken)
            return
        }

        refreshTokenSet { newTokenSet ->
            block(newTokenSet.accessToken)
        }
    }

    fun signInWithBrowser(
        context: Context,
        onComplete: (tokenSet: TokenSet) -> Unit,
    ) = AuthManager.start(
        context,
        BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
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
                logtoConfig = logtoConfig,
                logtoApiClient = logtoApiClient
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
    ) {
        val cachedTokenSet = tokenSet ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)
        val refreshToken = cachedTokenSet.refreshToken
            ?: throw LogtoException(LogtoException.REFRESH_TOKEN_IS_NOT_SUPPORTED)

        logtoApiClient.grantTokenByRefreshToken(
            clientId = logtoConfig.clientId,
            redirectUri = logtoConfig.redirectUri,
            refreshToken = refreshToken
        ) { tokenSet ->
            updateTokenSet(tokenSet)
            block(tokenSet)
        }
    }

    private var tokenSetStorage: TokenSetStorage? = null

    private val tokenSetStorageSharedPreferencesName =
        "$STORAGE_SHAREDPREFERENCES_NAME_PREFIX::${logtoConfig.cacheKey}"

    private var tokenSet: TokenSet? = null

    private var accessTokenExpiresAt: Long = 0L

    private fun updateTokenSet(updatedTokenSet: TokenSet?) {
        tokenSet = updatedTokenSet
        tokenSetStorage?.tokenSet = updatedTokenSet
        if (updatedTokenSet == null) {
            accessTokenExpiresAt = 0L
        } else {
            accessTokenExpiresAt = Utils.expiresAtFromNow(updatedTokenSet.expiresIn)
        }
    }

    private val logtoApiClient = LogtoApiClient(logtoConfig.domain, LogtoService())

    private companion object {
        private const val STORAGE_SHAREDPREFERENCES_NAME_PREFIX = "io.logto.android"
    }

    init {
        if (useStorage) {
            val storage = TokenSetStorage(application, tokenSetStorageSharedPreferencesName)
            tokenSetStorage = storage
            updateTokenSet(storage.tokenSet)
        }
    }
}
