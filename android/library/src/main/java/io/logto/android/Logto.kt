package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.auth.AuthManager
import io.logto.android.auth.browser.BrowserSignInFlow
import io.logto.android.auth.browser.BrowserSignOutFlow
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

    fun signInWithBrowser(
        context: Context,
        onComplete: (error: Error?, credential: Credential?) -> Unit
    ) {
        AuthManager.start(
            context,
            BrowserSignInFlow(
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
