package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoAndroidClient
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.EMPTY_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.INVALID_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.SIGN_OUT_FAILED

class BrowserSignOutFlow(
    private val idToken: String,
    private val logtoAndroidClient: LogtoAndroidClient,
    private val onComplete: (exception: LogtoException?) -> Unit,
) : IFlow {

    override fun start(context: Context) {
        try {
            logtoAndroidClient.getOidcConfiguration { oidcConfiguration ->
                val signOutUrl = logtoAndroidClient.getSignOutUrl(oidcConfiguration, idToken)
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

    override fun onResult(callbackUri: Uri) {
        try {
            validatePostLogoutRedirectUri(callbackUri)
            onComplete(null)
        } catch (exceptionOnValidate: LogtoException) {
            onComplete(exceptionOnValidate)
        }
    }

    @Suppress("ThrowsCount")
    private fun validatePostLogoutRedirectUri(uri: Uri) {
        if (uri.toString().isEmpty()) {
            throw LogtoException("$SIGN_OUT_FAILED: $EMPTY_REDIRECT_URI")
        }

        val errorDescription = uri.getQueryParameter(QueryKey.ERROR_DESCRIPTION)
        if (errorDescription != null) {
            throw LogtoException("$SIGN_OUT_FAILED: $errorDescription")
        }

        val error = uri.getQueryParameter(QueryKey.ERROR)
        if (error != null) {
            throw LogtoException("$SIGN_OUT_FAILED: $error")
        }

        if (!uri.toString().startsWith(logtoAndroidClient.logtoConfig.postLogoutRedirectUri)) {
            throw LogtoException("$SIGN_OUT_FAILED: $INVALID_REDIRECT_URI")
        }
    }
}
