package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserLoginFlow
import io.logto.android.auth.browser.BrowserLogoutFlow
import io.logto.android.config.LogtoConfig
import io.logto.android.model.Credential
import io.logto.android.storage.CredentialStorage

object Logto {
    private lateinit var application: Application

    private lateinit var logtoConfig: LogtoConfig

    private var credentialStorage: CredentialStorage? = null

    private var credentialCache: Credential? = null

    val credential: Credential?
        get() = credentialStorage?.getCredential() ?: credentialCache

    fun init(
        application: Application,
        logtoConfig: LogtoConfig,
        useStorage: Boolean = true
    ) {
        this.application = application
        this.logtoConfig = logtoConfig
        if (useStorage) {
            credentialStorage = CredentialStorage(application)
        }
    }

    fun loginWithBrowser(
        context: Context,
        onComplete: (error: Error?, credential: Credential?) -> Unit
    ) {
        checkInitState()
        AuthManager.start(
            context,
            BrowserLoginFlow(
                logtoConfig,
            ) { error, credential ->
                if (error == null && credential != null) {
                    credentialCache = credential
                    credentialStorage?.saveCredential(credential)
                }
                AuthManager.reset()
                onComplete(error, credential)
            }
        )
    }

    fun logoutWithBrowser(
        context: Context,
        onComplete: (error: Error?) -> Unit
    ) {
        checkInitState()
        credential?.let {
            AuthManager.start(
                context,
                BrowserLogoutFlow(
                    logtoConfig,
                    it.idToken,
                ) { error ->
                    if (error == null) {
                        clearCredential()
                    }
                    AuthManager.reset()
                    onComplete(error)
                }
            )
        }
    }

    private fun checkInitState() {
        if (!::application.isInitialized || !::logtoConfig.isInitialized) {
            throw Exception("Logto is not initialized!")
        }
    }

    private fun clearCredential() {
        credentialStorage?.clearCredential()
        credentialCache = null
    }
}
