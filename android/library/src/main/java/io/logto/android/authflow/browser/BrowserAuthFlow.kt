package io.logto.android.authflow.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.logto.android.api.LogtoService
import io.logto.android.callback.AuthenticationCallback
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.AuthConstant
import io.logto.android.pkce.Util
import io.logto.android.utils.UrlUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object BrowserAuthFlow {

    private var flowProperty: BrowserAuthFlowProperty? = null

    fun init(
        logtoConfig: LogtoConfig,
        authenticationCallback: AuthenticationCallback
    ): BrowserAuthFlow {
        resetFlow()

        val codeVerifier = Util.generateCodeVerifier()
        val authUrl = generateAuthUrl(logtoConfig, codeVerifier)

        flowProperty = BrowserAuthFlowProperty(
            codeVerifier,
            authUrl,
            logtoConfig,
            authenticationCallback
        )

        return this
    }

    fun startAuth(context: Context) {
        startAuthActivity(context)
    }

    fun onBrowserResult(redirectUri: Uri?) {
        flowProperty?.let { property ->
            if (redirectUri == null) {
                property.authenticationCallback
                    .onFailed(Error("onBrowserResult missing redirectUri!"))
                resetFlow()
                return
            }

            val authorizationCode = redirectUri.getQueryParameter(AuthConstant.QueryKey.CODE)

            if (authorizationCode == null) {
                property.authenticationCallback.onFailed(Error("Get authorization code failed!"))
                resetFlow()
                return
            }

            authorize(authorizationCode, property)
        } ?: throw Error("Browser auth flow missing flowProperty")
    }

    private fun resetFlow() {
        flowProperty = null
    }

    private fun startAuthActivity(context: Context) {
        flowProperty?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.authUrl))
            context.startActivity(intent)
        } ?: throw Exception("Browser auth flow is not initialized!")
    }

    private fun generateAuthUrl(logtoConfig: LogtoConfig, codeVerifier: String): String {
        val codeChallenge = Util.generateCodeChallenge(codeVerifier)
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

    private fun authorize(
        authorizationCode: String,
        property: BrowserAuthFlowProperty
    ) {
        property.apply {
            val logtoService = LogtoService.create(logtoConfig.oidcEndpoint)
            MainScope().launch {
                try {
                    val credential = logtoService.getCredential(
                        logtoConfig.redirectUri,
                        authorizationCode,
                        AuthConstant.GrantType.AUTHORIZATION_CODE,
                        logtoConfig.clientId,
                        codeVerifier,
                    )
                    authenticationCallback.onSuccess(credential)
                } catch (error: Error) {
                    authenticationCallback.onFailed(error)
                } finally {
                    resetFlow()
                }
            }
        }
    }
}
