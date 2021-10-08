package io.logto.android.auth.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.QueryKey
import io.logto.android.utils.UrlUtil

object BrowserLogoutFlow {

    private var logoutConfig: BrowserLogoutConfig? = null

    fun init(
        logtoConfig: LogtoConfig,
        idToken: String,
        onComplete: (error: Error?) -> Unit,
    ): BrowserLogoutFlow {
        resetFlow()

        logoutConfig = BrowserLogoutConfig(
            logtoConfig,
            idToken,
            onComplete
        )

        return this
    }

    fun logout(context: Context) {
        startLogoutActivity(context)
    }

    fun isInLogoutFlow(uri: Uri?): Boolean {
        return logoutConfig?.let { config ->
            return uri?.let {
                it.toString().startsWith(config.logtoConfig.postLogoutRedirectUri)
            } ?: false
        } ?: false
    }

    fun onBrowserResult() {
        logoutConfig?.let { config ->
            config.onComplete(null)
            resetFlow()
        } ?: throw Error("Browser logout flow missing config!")
    }

    private fun resetFlow() {
        logoutConfig = null
    }

    private fun startLogoutActivity(context: Context) {
        logoutConfig?.let {
            val logoutUrl = generateLogoutUrl(it)
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(logoutUrl)))
        } ?: throw Exception("Browser logout flow is not initialized!")
    }

    private fun generateLogoutUrl(logoutConfig: BrowserLogoutConfig): String {
        val baseUrl = Uri.parse(logoutConfig.logtoConfig.logoutEndpoint)
        val queries = mapOf(
            QueryKey.ID_TOKEN_HINT to logoutConfig.idToken,
            QueryKey.POST_LOGOUT_REDIRECT_URI to logoutConfig.logtoConfig.postLogoutRedirectUri,
        )
        return UrlUtil.appendQueryParameters(baseUrl.buildUpon(), queries).toString()
    }
}
