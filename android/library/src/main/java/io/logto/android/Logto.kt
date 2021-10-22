package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.api.LogtoService
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
import io.logto.android.config.LogtoConfig
import io.logto.android.model.TokenSet
import io.logto.android.storage.TokenSetStorage
import io.logto.android.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Logto(
    private val logtoConfig: LogtoConfig,
    application: Application,
    useStorage: Boolean = true
) {
    val tokenSet: TokenSet?
        get() = tokenSetStorage?.tokenSet ?: tokenSetCache

    fun signInWithBrowser(
        context: Context,
        onComplete: (error: Error?, tokenSet: TokenSet?) -> Unit
    ) {
        AuthManager.start(
            context,
            BrowserSignInFlow(
                logtoConfig,
                logtoService,
            ) { error, tokenSet ->
                if (error == null && tokenSet != null) {
                    updateTokenSet(tokenSet)
                }
                AuthManager.reset()
                onComplete(error, tokenSet)
            }
        )
    }

    fun signOutWithBrowser(
        context: Context,
        onComplete: (error: Error?) -> Unit
    ) {
        tokenSet?.let {
            AuthManager.start(
                context,
                BrowserSignOutFlow(
                    logtoConfig,
                    it.idToken,
                ) { error ->
                    if (error == null) {
                        updateTokenSet(null)
                    }
                    AuthManager.reset()
                    onComplete(error)
                }
            )
        } ?: onComplete(Error("Not authenticated"))
    }

    fun getAccessToken(
        block: (error: Error?, accessToken: String?) -> Unit
    ) {
        if (tokenSet == null) {
            block(Error("Not authenticated"), null)
            return
        }

        if (Utils.nowRoundToSec() < accessTokenExpiresAt) {
            block(null, tokenSet?.accessToken)
            return
        }

        refreshTokenSet { error, tokenSet ->
            block(error, tokenSet?.accessToken)
        }
    }

    fun refreshTokenSet(
        block: (error: Error?, tokenSet: TokenSet?) -> Unit
    ) {
        if (tokenSet == null) {
            block(Error("Not authenticated"), null)
            return
        }

        val refreshToken = tokenSet?.refreshToken
        if (refreshToken == null) {
            block(Error("Not Support Token Refresh!"), null)
            return
        }

        MainScope().launch {
            try {
                val updatedTokenSet = logtoService.grantTokenByRefreshToken(
                    clientId = logtoConfig.clientId,
                    redirectUri = logtoConfig.redirectUri,
                    refreshToken = refreshToken
                )
                updateTokenSet(updatedTokenSet)
                block(null, updatedTokenSet)
            } catch (error: Error) {
                block(error, null)
            }
        }
    }

    private val logtoService = LogtoService.create(logtoConfig.oidcEndpoint)

    private var tokenSetStorage: TokenSetStorage? = null

    private var tokenSetCache: TokenSet? = null
        set(value) {
            if (value != null) {
                field = tokenSetCache
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

    init {
        if (useStorage) {
            tokenSetStorage = TokenSetStorage(application, logtoConfig)
        }
    }
}
