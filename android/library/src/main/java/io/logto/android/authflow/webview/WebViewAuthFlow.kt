package io.logto.android.authflow.webview

import android.content.Context
import android.net.Uri
import io.logto.android.activity.WebViewAuthActivity
import io.logto.android.api.LogtoService
import io.logto.android.callback.AuthenticationCallback
import io.logto.android.callback.AuthorizationCodeCallback
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.AuthConstant
import io.logto.android.model.Credential
import io.logto.android.utils.PkceUtil
import io.logto.android.utils.UrlUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebViewAuthFlow(
    private val context: Context,
    private val logtoConfig: LogtoConfig,
    private val authenticationCallback: AuthenticationCallback
) {

    private val codeVerifier: String = PkceUtil.generateCodeVerifier()
    private val logtoService = LogtoService.create(logtoConfig.oidcEndpoint)

    fun startAuth() {
        WebViewAuthActivity.setAuthorizationCodeCallback(object : AuthorizationCodeCallback {
            override fun onSuccess(result: String) {
                authenticate(result, authenticationCallback)
            }

            override fun onFailed(error: Error) {
                authenticationCallback.onFailed(Error("request authorization code failed"))
            }
        })
        context.startActivity(
            WebViewAuthActivity.makeIntent(
                context,
                generateAuthUrl(),
                logtoConfig.redirectUri
            )
        )
    }

    private fun generateAuthUrl(): String {
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)
        val baseUrl = Uri.parse(logtoConfig.authEndpoint)
        val parameters = mapOf(
            AuthConstant.QueryKey.CLIENT_ID to logtoConfig.clientId,
            AuthConstant.QueryKey.CODE_CHALLENGE to codeChallenge,
            AuthConstant.QueryKey.CODE_CHALLENGE_METHOD to AuthConstant.CodeChallengeMethod.S256,
            AuthConstant.QueryKey.PROMPT to AuthConstant.PromptValue.CONSENT,
            AuthConstant.QueryKey.REDIRECT_URI to logtoConfig.redirectUri,
            AuthConstant.QueryKey.RESPONSE_TYPE to AuthConstant.ResponseType.CODE,
            AuthConstant.QueryKey.SCOPE to logtoConfig.encodedScopes,
            AuthConstant.QueryKey.RESOURCE to AuthConstant.ResourceValue.LOGTO_API,
        )
        return UrlUtil.appendQueryParameters(baseUrl.buildUpon(), parameters).toString()
    }

    private fun authenticate(
        authorizationCode: String,
        authenticationCallback: AuthenticationCallback
    ) {
        MainScope().launch {
            try {
                val credential = fetchCredential(authorizationCode)
                authenticationCallback.onSuccess(credential)
            } catch (error: Error) {
                authenticationCallback.onFailed(error)
            }
        }
    }

    private suspend fun fetchCredential(authorizationCode: String): Credential = withContext(
        Dispatchers.IO
    ) {
        val response = logtoService.getCredential(
            logtoConfig.redirectUri,
            authorizationCode,
            AuthConstant.GrantType.AUTHORIZATION_CODE,
            logtoConfig.clientId,
            codeVerifier,
        )

        if (!response.isSuccessful) {
            throw Error("request credential call error")
        }

        response.body() ?: throw Error("fetch credential error")
    }

    companion object {
        private val TAG = WebViewAuthFlow::class.java.simpleName
    }
}
