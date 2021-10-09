package io.logto.android.auth.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.QueryKey
import io.logto.android.utils.UrlUtil

class BrowserSignOutFlow(
    private val logtoConfig: LogtoConfig,
    private val idToken: String,
    private val onComplete: (error: Error?) -> Unit,
) : IFlow {

    override fun start(context: Context) {
        startSignOutActivity(context)
    }

    override fun onResult(data: Uri) {
        if (!data.toString().startsWith(logtoConfig.postLogoutRedirectUri)) {
            onComplete(Error("Sign out failed!"))
            return
        }
        onComplete(null)
    }

    private fun startSignOutActivity(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(generateSignOutUrl())))
    }

    private fun generateSignOutUrl(): String {
        val baseUrl = Uri.parse(logtoConfig.signOutEndpoint)
        val queries = mapOf(
            QueryKey.ID_TOKEN_HINT to idToken,
            QueryKey.POST_LOGOUT_REDIRECT_URI to logtoConfig.postLogoutRedirectUri,
        )
        return UrlUtil.appendQueryParameters(baseUrl.buildUpon(), queries).toString()
    }
}
