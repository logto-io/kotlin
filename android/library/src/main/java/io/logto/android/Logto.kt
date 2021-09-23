package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.authflow.webview.WebViewAuthFlow
import io.logto.android.callback.AuthenticationCallback
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
        useStorage: Boolean = false
    ) {
        this.application = application
        this.logtoConfig = logtoConfig
        if (useStorage) {
            credentialStorage = CredentialStorage(application)
        }
    }

    fun loginWithWebView(
        context: Context,
        onComplete: (error: Error?, credential: Credential?) -> Unit
    ) {
        checkInitState()
        WebViewAuthFlow(
            context,
            logtoConfig,
            object : AuthenticationCallback {
                override fun onSuccess(result: Credential) {
                    credentialCache = result
                    credentialStorage?.saveCredential(result)
                    onComplete(null, result)
                }

                override fun onFailed(error: Error) {
                    onComplete(error, null)
                }
            }
        ).startAuth()
    }

    private fun checkInitState() {
        if (!::application.isInitialized || !::logtoConfig.isInitialized) {
            throw Exception("Logto is not initialized!")
        }
    }
}
