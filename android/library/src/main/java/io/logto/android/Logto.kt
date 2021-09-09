package io.logto.android

import android.content.Context
import io.logto.android.authflow.webview.WebViewAuthFlow
import io.logto.android.model.Credential
import io.logto.android.storage.CredentialStorage

class Logto {
    companion object {
        private var credentialStorage: CredentialStorage? = null

        fun webViewAuthFlow(context: Context): WebViewAuthFlow {
            credentialStorage = CredentialStorage(context.applicationContext)
            return WebViewAuthFlow(context, credentialStorage)
        }

        fun getCredential(): Credential? = credentialStorage?.getCredential()
    }
}
