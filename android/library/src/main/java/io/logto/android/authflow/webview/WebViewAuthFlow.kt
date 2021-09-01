package io.logto.android.authflow.webview

import android.app.Activity
import android.net.Uri
import android.util.Log
import io.logto.android.LogtoConfig
import io.logto.android.activity.WebViewAuthActivity
import io.logto.android.callback.AuthenticationCallback
import io.logto.android.callback.AuthorizationCodeCallback
import io.logto.android.client.LogtoClientBuilder
import io.logto.android.client.api.LogtoClient
import io.logto.android.constant.AuthConstant
import io.logto.android.model.Credential
import io.logto.android.utils.PkceUtil
import io.logto.android.utils.UrlUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WebViewAuthFlow {

    private lateinit var logtoConfig: LogtoConfig
    private lateinit var logtoClient: LogtoClient
    private lateinit var codeVerifier: String

    fun login(logtoConfig: LogtoConfig): WebViewAuthFlow {
        this.logtoConfig = logtoConfig
        logtoClient = LogtoClientBuilder(logtoConfig).build()
        codeVerifier = PkceUtil.generateCodeVerifier()
        return this
    }

    fun start(context: Activity, authenticationCallback: AuthenticationCallback) {
        if (!this::logtoClient.isInitialized) {
            // LOG-67: Catch exceptions in WebView auth flow
            authenticationCallback.onFailed(Error("missing logtoConfig"))
            return
        }

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
        val call = logtoClient.getCredential(
            logtoConfig.redirectUri,
            authorizationCode,
            AuthConstant.GrantType.AUTHORIZATION_CODE,
            logtoConfig.clientId,
            codeVerifier,
        )

        call.enqueue(object : Callback<Credential> {
            override fun onResponse(call: Call<Credential>, response: Response<Credential>) {
                val credential = response.body()
                if (credential != null) {
                    authenticationCallback.onSuccess(credential)
                    return
                }
                authenticationCallback.onFailed(Error("fetch credential error"))
            }

            override fun onFailure(call: Call<Credential>, t: Throwable) {
                authenticationCallback.onFailed(Error("authentication request error"))
            }
        })
    }

    companion object {
        private val TAG = WebViewAuthFlow::class.java.simpleName
    }
}
