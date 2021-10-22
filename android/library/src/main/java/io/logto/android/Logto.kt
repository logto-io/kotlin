package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
import io.logto.android.client.LogtoClient
import io.logto.android.config.LogtoConfig
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import io.logto.android.storage.TokenSetStorage
import io.logto.android.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Logto(
    private val logtoConfig: LogtoConfig,
    private val logtoUrl: String,
    application: Application,
    useStorage: Boolean = true,
) {
    val tokenSet: TokenSet?
        get() = tokenSetStorage?.tokenSet ?: tokenSetCache

    fun signInWithBrowser(
        context: Context,
        onComplete: (error: Error?, tokenSet: TokenSet?) -> Unit
    ) = discoverIfNeeded { oidcConfiguration ->
        AuthManager.start(
            context,
            BrowserSignInFlow(
                logtoConfig,
                oidcConfiguration,
                logtoClient,
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
    ) = discoverIfNeeded { oidcConfig ->
        tokenSet?.let {
            AuthManager.start(
                context,
                BrowserSignOutFlow(
                    idToken = it.idToken,
                    endSessionEndpoint = oidcConfig.endSessionEndpoint,
                    postLogoutRedirectUri = logtoConfig.postLogoutRedirectUri,
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

        discoverIfNeeded { oidcConfig ->
            MainScope().launch {
                try {
                    val updatedTokenSet = logtoClient.grantTokenByRefreshToken(
                        tokenEndpoint = oidcConfig.tokenEndpoint,
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
    }

    private val logtoClient = LogtoClient()

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

    private var oidcConfigurationCache: OidcConfiguration? = null
    private fun discoverIfNeeded(block: (oidcConfig: OidcConfiguration) -> Unit) {
        oidcConfigurationCache?.let {
            block(it)
            return
        }

        MainScope().launch {
            try {
                val fetchedOidcConfig = logtoClient.discover(logtoUrl)
                oidcConfigurationCache = fetchedOidcConfig
                block(fetchedOidcConfig)
            } catch (error: Error) {
                throw error
            }
        }
    }

    init {
        if (useStorage) {
            tokenSetStorage = TokenSetStorage(application, logtoConfig)
        }
    }
}
