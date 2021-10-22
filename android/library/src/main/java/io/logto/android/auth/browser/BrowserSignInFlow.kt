package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoClient
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.CodeChallengeMethod
import io.logto.android.constant.PromptValue
import io.logto.android.constant.QueryKey
import io.logto.android.constant.ResourceValue
import io.logto.android.constant.ResponseType
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import io.logto.android.pkce.Util
import io.logto.android.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BrowserSignInFlow(
    private val logtoConfig: LogtoConfig,
    private val oidcConfiguration: OidcConfiguration,
    private val logtoClient: LogtoClient,
    private val onComplete: (error: Error?, tokenSet: TokenSet?) -> Unit
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
        val baseUrl = Uri.parse(oidcConfiguration.authorizationEndpoint)
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
                val tokenSet = logtoClient.grantTokenByAuthorizationCode(
                    tokenEndpoint = oidcConfiguration.tokenEndpoint,
                    clientId = logtoConfig.clientId,
                    redirectUri = logtoConfig.redirectUri,
                    code = authorizationCode,
                    codeVerifier = codeVerifier,
                )
                onComplete(null, tokenSet)
            } catch (error: Error) {
                onComplete(error, null)
            }
        }
    }
}
