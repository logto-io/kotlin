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
    val tokenSet: TokenSet?
        get() = tokenSetStorage?.tokenSet ?: tokenSetCache

    val isAuthenticated: Boolean
        get() = tokenSet != null

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

    fun getAccessToken(block: (accessToken: String) -> Unit) {
        val currentTokenSet = tokenSet ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)

        if (Utils.nowRoundToSec() < accessTokenExpiresAt) {
            block(currentTokenSet.accessToken)
            return
        }

        refreshTokenSet {
            block(it.accessToken)
        }
    }

    fun refreshTokenSet(
        block: (tokenSet: TokenSet) -> Unit,
    ) {
        val currentTokenSet = tokenSet ?: throw LogtoException(LogtoException.NOT_AUTHENTICATED)

        val refreshToken = currentTokenSet.refreshToken
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

    private var tokenSetCache: TokenSet? = null
        set(value) {
            if (value != null) {
                field = value
                accessTokenExpiresAt = Utils.expiresAtFromNow(value.expiresIn)
            } else {
                field = null
                accessTokenExpiresAt = 0L
            }
        }

    private var accessTokenExpiresAt: Long = 0L

    private fun updateTokenSet(tokenSet: TokenSet?) {
        tokenSetCache = tokenSet
        tokenSetStorage?.tokenSet = tokenSet
    }

    private val logtoApiClient = LogtoApiClient(logtoConfig.domain, LogtoService())

    private companion object {
        private const val STORAGE_SHAREDPREFERENCES_NAME_PREFIX = "io.logto.android"
    }

    init {
        if (useStorage) {
            tokenSetStorage = TokenSetStorage(application, tokenSetStorageSharedPreferencesName)
        }
    }
}
