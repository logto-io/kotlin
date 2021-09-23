package io.logto.android

import android.app.Application
import android.content.Context
import io.logto.android.authflow.webview.WebViewAuthFlow
import io.logto.android.callback.AuthenticationCallback
import io.logto.android.config.LogtoConfig
import io.logto.android.model.Credential
import io.logto.android.storage.CredentialStorage

class Logto private constructor() {

    private lateinit var logtoConfig: LogtoConfig

    private lateinit var application: Application

    private var inited: Boolean = false

    private var useStorage: Boolean = false

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
        this.useStorage = useStorage
        if (this.useStorage) {
            credentialStorage = CredentialStorage(application)
        }
        inited = true
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
        if (!inited) {
            throw Exception("Logto singleton is not initialized!")
        }
    }

    companion object {
        private lateinit var INSTANCE: Logto

        fun getInstance(): Logto {
            synchronized(Logto::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Logto()
                }
                return INSTANCE
            }
        }
    }
}
