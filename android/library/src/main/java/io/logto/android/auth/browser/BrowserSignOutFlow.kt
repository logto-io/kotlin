package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.constant.QueryKey
import io.logto.android.utils.Utils

class BrowserSignOutFlow(
    private val idToken: String,
    private val endSessionEndpoint: String,
    private val postLogoutRedirectUri: String,
    private val onComplete: (error: Error?) -> Unit,
) : IFlow {

    override fun start(context: Context) {
        startSignOutActivity(context)
    }

    override fun onResult(data: Uri) {
        if (!data.toString().startsWith(postLogoutRedirectUri)) {
            onComplete(Error("Sign out failed!"))
            return
        }
        onComplete(null)
    }

    private fun startSignOutActivity(context: Context) {
        context.startActivity(
            AuthorizationActivity.createHandleStartIntent(
                context,
                generateSignOutUrl(
                    endSessionEndpoint,
                    idToken,
                    postLogoutRedirectUri,
                ),
            )
        )
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
