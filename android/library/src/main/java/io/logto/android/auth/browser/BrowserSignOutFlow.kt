package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.QueryKey
import io.logto.android.utils.Utils

class BrowserSignOutFlow(
    private val idToken: String,
    private val logtoConfig: LogtoConfig,
    private val logtoApiClient: LogtoApiClient,
    private val onComplete: (exception: Exception?) -> Unit,
) : IFlow {

    override fun start(context: Context) {
        startSignOutActivity(context)
    }

    override fun onResult(data: Uri) {
        if (!data.toString().startsWith(logtoConfig.postLogoutRedirectUri)) {
            onComplete(Exception("Sign out failed!"))
            return
        }
        onComplete(null)
    }

    private fun startSignOutActivity(context: Context) {
        logtoApiClient.discover { oidcConfig ->
            context.startActivity(
                AuthorizationActivity.createHandleStartIntent(
                    context,
                    generateSignOutUrl(
                        oidcConfig.endSessionEndpoint,
                        idToken,
                        logtoConfig.postLogoutRedirectUri,
                    ),
                )
            )
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
