package io.logto.android.authflow.webview

import android.app.Activity
import android.net.Uri
import io.logto.android.LogtoConfig
import io.logto.android.activity.WebViewAuthActivity
import io.logto.android.constant.AuthConstant
import io.logto.android.utils.PkceUtil
import io.logto.android.utils.UrlUtil

class WebViewAuthFlow {

    private lateinit var logtoConfig: LogtoConfig

    fun login(logtoConfig: LogtoConfig): WebViewAuthFlow {
        this.logtoConfig = logtoConfig
        return this
    }

    fun start(context: Activity) {
        context.startActivity(
            WebViewAuthActivity.makeIntent(
                context,
                generateAuthUrl(),
                logtoConfig.redirectUri
            )
        )
    }

    private fun generateAuthUrl(): String {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)
        val baseUrl = Uri.parse(logtoConfig.authEndpoint)
        val parameters = mapOf(
            AuthConstant.KEY_CLIENT_ID to logtoConfig.clientId,
            AuthConstant.KEY_CODE_CHALLENGE to codeChallenge,
            AuthConstant.KEY_CODE_CHALLENGE_METHOD to AuthConstant.CodeChallengeMethod.S256,
            AuthConstant.KEY_PROMPT to AuthConstant.PromptValue.CONSENT,
            AuthConstant.KEY_REDIRECT_URI to logtoConfig.redirectUri,
            AuthConstant.KEY_RESPONSE_TYPE to AuthConstant.ResponseType.CODE,
            AuthConstant.KEY_SCOPE to logtoConfig.encodedScopes,
            AuthConstant.KEY_RESOURCE to AuthConstant.ResourceValue.LOGTO_API,
        )
        return UrlUtil.appendQueryParameters(baseUrl.buildUpon(), parameters).toString()
    }
}
