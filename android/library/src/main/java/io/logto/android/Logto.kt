package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserLoginFlow
import io.logto.android.auth.browser.BrowserLogoutFlow
import io.logto.android.config.LogtoConfig
import io.logto.android.model.Credential
import io.logto.android.storage.CredentialStorage

class Logto(
    private val logtoConfig: LogtoConfig,
    application: Application,
    useStorage: Boolean = true
) {
    private var credentialStorage: CredentialStorage? = null

    private var credentialCache: Credential? = null

    val credential: Credential?
        get() = credentialStorage?.getCredential() ?: credentialCache

    init {
        if (useStorage) {
            credentialStorage = CredentialStorage(application, logtoConfig)
        }
    }

    fun loginWithBrowser(
        context: Context,
        onComplete: (error: Error?, credential: Credential?) -> Unit
    ) {
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
        } ?: onComplete(Error("You are not signed in!"))
    }

    private fun clearCredential() {
        credentialStorage?.clearCredential()
        credentialCache = null
    }
}
