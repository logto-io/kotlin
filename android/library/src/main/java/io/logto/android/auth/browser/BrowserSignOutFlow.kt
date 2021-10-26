package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.QueryKey
import io.logto.android.exception.LogtoException
import io.logto.android.utils.Utils

class BrowserSignOutFlow(
    private val idToken: String,
    private val logtoConfig: LogtoConfig,
    private val logtoApiClient: LogtoApiClient,
    private val onComplete: (exception: LogtoException?) -> Unit,
) : IFlow {

    override fun start(context: Context) {
        startSignOutActivity(context)
    }

    override fun onResult(data: Uri) {
        val redirectUri = data.toString()
        if (redirectUri.isEmpty() ||
            !redirectUri.startsWith(logtoConfig.postLogoutRedirectUri)
        ) {
            val error = data.getQueryParameter(QueryKey.ERROR)
            onComplete(LogtoException("${LogtoException.SIGN_OUT_FAILED}: $error"))
            return
        }
        onComplete(null)
    }

    private fun startSignOutActivity(context: Context) {
        logtoApiClient.discover { exception, oidcConfiguration ->
            if (exception != null || oidcConfiguration == null) {
                onComplete(exception)
                return@discover
            }
            val signOutUrl = generateSignOutUrl(
                oidcConfiguration.endSessionEndpoint,
                idToken,
                logtoConfig.postLogoutRedirectUri,
            )
            val intent = AuthorizationActivity.createHandleStartIntent(
                context,
                signOutUrl,
            )
            context.startActivity(intent)
        }
    }

    private fun generateSignOutUrl(
        endSessionEndpoint: String,
        idToken: String,
        postLogoutRedirectUri: String,
    ): String {
        val baseUrl = Uri.parse(endSessionEndpoint)
        val queries = mapOf(
            QueryKey.ID_TOKEN_HINT to idToken,
            QueryKey.POST_LOGOUT_REDIRECT_URI to postLogoutRedirectUri,
        )
        return Utils.appendQueryParameters(baseUrl.buildUpon(), queries).toString()
    }
}
