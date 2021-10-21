package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.api.LogtoService
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.CodeChallengeMethod
import io.logto.android.constant.PromptValue
import io.logto.android.constant.QueryKey
import io.logto.android.constant.ResourceValue
import io.logto.android.constant.ResponseType
import io.logto.android.model.Credential
import io.logto.android.pkce.Util
import io.logto.android.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BrowserSignInFlow(
    private val logtoConfig: LogtoConfig,
    private val logtoService: LogtoService,
    private val onComplete: (error: Error?, credential: Credential?) -> Unit
) : IFlow {

    private val codeVerifier: String = Util.generateCodeVerifier()

    override fun start(context: Context) {
        startAuthorizationActivity(context)
    }

    override fun onResult(data: Uri) {
        val authorizationCode = data.getQueryParameter(QueryKey.CODE)
        if (authorizationCode == null ||
            !data.toString().startsWith(logtoConfig.redirectUri)
        ) {
            onComplete(Error("Get authorization code failed!"), null)
            return
        }
        authorize(authorizationCode)
    }

    private fun startAuthorizationActivity(context: Context) {
        val intent = AuthorizationActivity.createHandleStartIntent(context, generateAuthUrl())
        context.startActivity(intent)
    }

    private fun generateAuthUrl(): String {
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
        return Utils.appendQueryParameters(baseUrl.buildUpon(), queries).toString()
    }

    private fun authorize(
        authorizationCode: String,
    ) {
        MainScope().launch {
            try {
                val credential = logtoService.exchangeCredential(
                    clientId = logtoConfig.clientId,
                    redirectUri = logtoConfig.redirectUri,
                    code = authorizationCode,
                    codeVerifier = codeVerifier,
                )
                onComplete(null, credential)
            } catch (error: Error) {
                onComplete(error, null)
            }
        }
    }
}
