package io.logto.android

import android.content.Context
import io.logto.android.authflow.webview.WebViewAuthFlow
import io.logto.android.model.Credential
import io.logto.android.storage.CredentialStorage

class Logto {
    companion object {
        lateinit var credentialStorage: CredentialStorage

        fun webViewAuthFlow(context: Context): WebViewAuthFlow {
            credentialStorage = CredentialStorage(context.applicationContext)
            return WebViewAuthFlow(context, credentialStorage)
        }

        fun getCredential(): Credential? {
            return if (this::credentialStorage.isInitialized) {
                credentialStorage.getCredential()
            } else {
                null
            }
        }
    }
}
