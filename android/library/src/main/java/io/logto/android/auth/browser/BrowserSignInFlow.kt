package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoApiClient
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.CodeChallengeMethod
import io.logto.android.constant.PromptValue
import io.logto.android.constant.QueryKey
import io.logto.android.constant.ResourceValue
import io.logto.android.constant.ResponseType
import io.logto.android.exception.LogtoException
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import io.logto.android.pkce.Util
import io.logto.android.utils.Utils

class BrowserSignInFlow(
    private val logtoConfig: LogtoConfig,
    private val logtoApiClient: LogtoApiClient,
    private val onComplete: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
) : IFlow {

    private val codeVerifier: String = Util.generateCodeVerifier()

    override fun start(context: Context) {
        startAuthorizationActivity(context)
    }

    override fun onResult(data: Uri) {
        val authorizationCode = data.getQueryParameter(QueryKey.CODE)
        if (authorizationCode.isNullOrEmpty() ||
            !data.toString().startsWith(logtoConfig.redirectUri)
        ) {
            val error = data.getQueryParameter(QueryKey.ERROR)
            onComplete(
                LogtoException("${LogtoException.SIGN_IN_FAILED}: $error"),
                null,
            )
            return
        }
        grantTokenByAuthorizationCode(authorizationCode)
    }

    private fun startAuthorizationActivity(context: Context) {
        logtoApiClient.discover { discoverException, oidcConfiguration ->
            if (discoverException != null || oidcConfiguration == null) {
                onComplete(discoverException, null)
                return@discover
            }
            try {
                val codeChallenge = Util.generateCodeChallenge(codeVerifier)
                val intent = AuthorizationActivity.createHandleStartIntent(
                    context,
                    generateAuthUrl(oidcConfiguration, codeChallenge),
                )
                context.startActivity(intent)
            } catch (exception: LogtoException) {
                onComplete(exception, null)
            }
        }
    }

    private fun generateAuthUrl(
        oidcConfiguration: OidcConfiguration,
        codeChallenge: String,
    ): String {
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

    private fun grantTokenByAuthorizationCode(
        authorizationCode: String,
    ) {
        logtoApiClient.grantTokenByAuthorizationCode(
            clientId = logtoConfig.clientId,
            redirectUri = logtoConfig.redirectUri,
            code = authorizationCode,
            codeVerifier = codeVerifier,
        ) { exception, tokenSet ->
            onComplete(exception, tokenSet)
        }
    }
}
