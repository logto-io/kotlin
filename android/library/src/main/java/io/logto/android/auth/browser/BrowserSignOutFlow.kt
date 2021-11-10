package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.SIGN_OUT_FAILED

class BrowserSignOutFlow(
    private val idToken: String,
    private val logtoAndroidClient: LogtoAndroidClient,
    private val onComplete: (exception: LogtoException?) -> Unit,
) : IFlow {

    override fun start(context: Context) {
        try {
            logtoAndroidClient.getOidcConfigurationAsync { oidcConfiguration ->
                val signOutUrl = logtoAndroidClient.getSignOutUrl(
                    oidcConfiguration.endSessionEndpoint,
                    idToken
                )
                val intent = AuthorizationActivity.createHandleStartIntent(
                    context = context,
                    endpoint = signOutUrl,
                    redirectUri = logtoAndroidClient.logtoConfig.postLogoutRedirectUri,
                )
                context.startActivity(intent)
            }
        } catch (exception: LogtoException) {
            onComplete(exception)
        }
    }

    override fun handleRedirectUri(redirectUri: Uri) {
        val exceptionMsg = Utils.validateRedirectUri(
            redirectUri,
            logtoAndroidClient.logtoConfig.postLogoutRedirectUri
        )
        if (exceptionMsg != null) {
            onComplete(LogtoException("$SIGN_OUT_FAILED: $exceptionMsg"))
            return
        }
        onComplete(null)
    }
}
