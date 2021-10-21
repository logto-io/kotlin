package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.api.LogtoService
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
import io.logto.android.config.LogtoConfig
import io.logto.android.model.Credential
import io.logto.android.storage.CredentialStorage
import io.logto.android.utils.TokenUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Logto(
    private val logtoConfig: LogtoConfig,
    application: Application,
    useStorage: Boolean = true
) {
    val credential: Credential?
        get() = credentialStorage?.getCredential() ?: credentialCache

    fun signInWithBrowser(
        context: Context,
        onComplete: (error: Error?, credential: Credential?) -> Unit
    ) {
        AuthManager.start(
            context,
            BrowserSignInFlow(
                logtoConfig,
                logtoService,
            ) { error, credential ->
                if (error == null && credential != null) {
                    updateCredential(credential)
                }
                AuthManager.reset()
                onComplete(error, credential)
            }
        )
    }

    fun signOutWithBrowser(
        context: Context,
        onComplete: (error: Error?) -> Unit
    ) {
        credential?.let {
            AuthManager.start(
                context,
                BrowserSignOutFlow(
                    logtoConfig,
                    it.idToken,
                ) { error ->
                    if (error == null) {
                        updateCredential(null)
                    }
                    AuthManager.reset()
                    onComplete(error)
                }
            )
        } ?: onComplete(Error("You are not signed in!"))
    }

    fun getAccessToken(
        block: (error: Error?, accessToken: String?) -> Unit
    ) {
        if (credential == null) {
            block(Error("Not Sign In!"), null)
            return
        }

        if (System.currentTimeMillis() < accessTokenExpiresAt) {
            block(null, credential?.accessToken)
            return
        }

        refreshCredential { error, credential ->
            block(error, credential?.accessToken)
        }
    }

    fun refreshCredential(
        block: (error: Error?, credential: Credential?) -> Unit
    ) {
        if (credential == null) {
            block(Error("Not Sign In!"), null)
            return
        }

        val refreshToken = credential?.refreshToken
        if (refreshToken == null) {
            block(Error("Not Support Token Refresh!"), null)
            return
        }

        MainScope().launch {
            try {
                val updatedCredential = logtoService.refreshCredential(
                    clientId = logtoConfig.clientId,
                    redirectUri = logtoConfig.redirectUri,
                    refreshToken = refreshToken
                )
                updateCredential(updatedCredential)
                block(null, updatedCredential)
            } catch (error: Error) {
                block(error, null)
            }
        }
    }

    private val logtoService = LogtoService.create(logtoConfig.oidcEndpoint)

    private var credentialStorage: CredentialStorage? = null

    private var credentialCache: Credential? = null
        set(value) {
            if (value != null) {
                field = credentialCache
                accessTokenExpiresAt = TokenUtil.expiresAt(value)
            } else {
                field = null
                accessTokenExpiresAt = 0L
            }
        }

    private var accessTokenExpiresAt: Long = 0L

    private fun updateCredential(credential: Credential?) {
        credentialCache = credential
        if (credential == null) {
            credentialStorage?.clearCredential()
        } else {
            credentialStorage?.saveCredential(credential)
        }
    }

    init {
        if (useStorage) {
            credentialStorage = CredentialStorage(application, logtoConfig)
        }
    }
}
