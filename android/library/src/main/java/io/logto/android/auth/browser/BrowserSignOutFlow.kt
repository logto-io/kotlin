package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.callback.HandleLogtoExceptionCallback
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.SIGN_OUT_FAILED

class BrowserSignOutFlow(
    private val idToken: String,
    private val logtoAndroidClient: LogtoAndroidClient,
    private val onComplete: HandleLogtoExceptionCallback,
) : IFlow {

    override fun start(context: Context) {
        logtoAndroidClient.getOidcConfigurationAsync { exception, oidcConfiguration ->
            if (exception != null) {
                onComplete(exception)
                return@getOidcConfigurationAsync
            }
            val signOutUrl = logtoAndroidClient.getSignOutUrl(
                oidcConfiguration!!.endSessionEndpoint,
                idToken
            )
            val intent = AuthorizationActivity.createHandleStartIntent(
                context = context,
                endpoint = signOutUrl,
                redirectUri = logtoAndroidClient.logtoConfig.postLogoutRedirectUri,
            )
            context.startActivity(intent)
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
