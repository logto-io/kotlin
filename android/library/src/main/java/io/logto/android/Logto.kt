package io.logto.android

import android.app.Application
import android.content.Context
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

    fun signInWithBrowser(
        context: Context,
        onComplete: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
    ) = AuthManager.start(
        context,
        BrowserSignInFlow(
            logtoConfig,
            logtoApiClient,
        ) { exception, tokenSet ->
            if (exception == null && tokenSet != null) {
                updateTokenSet(tokenSet)
            }
            AuthManager.reset()
            onComplete(exception, tokenSet)
        }
    )

    fun signOutWithBrowser(
        context: Context,
        onComplete: (exception: LogtoException?) -> Unit
    ) = tokenSet?.let {
        AuthManager.start(
            context,
            BrowserSignOutFlow(
                idToken = it.idToken,
                logtoConfig = logtoConfig,
                logtoApiClient = logtoApiClient
            ) { exception ->
                if (exception == null) {
                    updateTokenSet(null)
                }
                AuthManager.reset()
                onComplete(exception)
            }
        )
    } ?: onComplete(LogtoException(LogtoException.NOT_AUTHENTICATED))

    fun getAccessToken(
        block: (exception: LogtoException?, accessToken: String?) -> Unit
    ) {
        if (tokenSet == null) {
            block(LogtoException(LogtoException.NOT_AUTHENTICATED), null)
            return
        }

        if (Utils.nowRoundToSec() < accessTokenExpiresAt) {
            block(null, tokenSet?.accessToken)
            return
        }

        refreshTokenSet { exception, tokenSet ->
            block(exception, tokenSet?.accessToken)
        }
    }

    fun refreshTokenSet(
        block: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
    ) {
        if (tokenSet == null) {
            block(LogtoException(LogtoException.NOT_AUTHENTICATED), null)
            return
        }

        val refreshToken = tokenSet?.refreshToken
        if (refreshToken == null) {
            block(LogtoException(LogtoException.REFRESH_TOKEN_IS_NOT_SUPPORTED), null)
            return
        }

        logtoApiClient.grantTokenByRefreshToken(
            clientId = logtoConfig.clientId,
            redirectUri = logtoConfig.redirectUri,
            refreshToken = refreshToken
        ) { exception, tokenSet ->
            updateTokenSet(tokenSet)
            block(exception, tokenSet)
        }
    }

    private var tokenSetStorage: TokenSetStorage? = null

    private var tokenSetCache: TokenSet? = null
        set(value) {
            if (value != null) {
                field = value
                accessTokenExpiresAt = Utils.expiresAt(value)
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

    private val logtoApiClient = LogtoApiClient(logtoConfig.logtoUrl)

    init {
        if (useStorage) {
            tokenSetStorage = TokenSetStorage(application, logtoConfig)
        }
    }
}
