package io.logto.android.auth.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.logto.android.api.LogtoService
import io.logto.android.callback.AuthenticationCallback
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.CodeChallengeMethod
import io.logto.android.constant.GrantType
import io.logto.android.constant.PromptValue
import io.logto.android.constant.QueryKey
import io.logto.android.constant.ResourceValue
import io.logto.android.constant.ResponseType
import io.logto.android.pkce.Util
import io.logto.android.utils.UrlUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object BrowserLoginFlow {

    private var loginConfig: BrowserLoginConfig? = null

    fun init(
        logtoConfig: LogtoConfig,
        authenticationCallback: AuthenticationCallback
    ): BrowserLoginFlow {
        resetFlow()

        val codeVerifier = Util.generateCodeVerifier()
        val authUrl = generateAuthUrl(logtoConfig, codeVerifier)

        loginConfig = BrowserLoginConfig(
            codeVerifier,
            authUrl,
            logtoConfig,
            authenticationCallback
        )

        return this
    }

    fun login(context: Context) {
        startAuthActivity(context)
    }

    fun onBrowserResult(redirectUri: Uri?) {
        loginConfig?.let { config ->
            if (redirectUri == null) {
                config.authenticationCallback
                    .onFailed(Error("onBrowserResult missing redirectUri!"))
                resetFlow()
                return
            }

            val authorizationCode = redirectUri.getQueryParameter(QueryKey.CODE)

            if (authorizationCode == null) {
                config.authenticationCallback.onFailed(Error("Get authorization code failed!"))
                resetFlow()
                return
            }

            authorize(authorizationCode, config)
        } ?: throw Error("Browser auth flow missing config!")
    }

    private fun resetFlow() {
        loginConfig = null
    }

    private fun startAuthActivity(context: Context) {
        loginConfig?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.authUrl))
            context.startActivity(intent)
        } ?: throw Exception("Browser auth flow is not initialized!")
    }

    private fun generateAuthUrl(logtoConfig: LogtoConfig, codeVerifier: String): String {
        val codeChallenge = Util.generateCodeChallenge(codeVerifier)
        val baseUrl = Uri.parse(logtoConfig.authEndpoint)
        val queries = mapOf(
            QueryKey.CLIENT_ID to logtoConfig.clientId,
            QueryKey.CODE_CHALLENGE to codeChallenge,
            QueryKey.CODE_CHALLENGE_METHOD to CodeChallengeMethod.S256,
            QueryKey.PROMPT to PromptValue.CONSENT,
            QueryKey.REDIRECT_URI to logtoConfig.redirectUri,
            QueryKey.RESPONSE_TYPE to ResponseType.CODE,
            QueryKey.SCOPE to logtoConfig.encodedScopes,
            QueryKey.RESOURCE to ResourceValue.LOGTO_API,
        )
        return UrlUtil.appendQueryParameters(baseUrl.buildUpon(), queries).toString()
    }

    private fun authorize(
        authorizationCode: String,
        config: BrowserLoginConfig
    ) {
        config.apply {
            val logtoService = LogtoService.create(logtoConfig.oidcEndpoint)
            MainScope().launch {
                try {
                    val credential = logtoService.getCredential(
                        logtoConfig.redirectUri,
                        authorizationCode,
                        GrantType.AUTHORIZATION_CODE,
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
