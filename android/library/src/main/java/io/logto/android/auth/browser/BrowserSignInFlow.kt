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
import io.logto.android.pkce.Pkce
import io.logto.android.utils.Utils

class BrowserSignInFlow(
    private val logtoConfig: LogtoConfig,
    private val logtoApiClient: LogtoApiClient,
    private val onComplete: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
) : IFlow {
    private val codeVerifier: String = Pkce.generateCodeVerifier()

    override fun start(context: Context) {
        startAuthorizationActivity(context)
    }

    override fun onResult(data: Uri) {
        val validAuthorizationCode = ensureValidAuthorizationCode(data, logtoConfig.redirectUri)
        if (validAuthorizationCode == null) {
            val errorDesc = data.getQueryParameter(QueryKey.ERROR_DESCRIPTION)
            onComplete(LogtoException("${LogtoException.SIGN_IN_FAILED}: $errorDesc"), null)
            return
        }

        try {
            logtoApiClient.grantTokenByAuthorizationCode(
                clientId = logtoConfig.clientId,
                redirectUri = logtoConfig.redirectUri,
                code = validAuthorizationCode,
                codeVerifier = codeVerifier,
            ) {
                onComplete(null, it)
            }
        } catch (exception: LogtoException) {
            onComplete(exception, null)
        }
    }

    private fun startAuthorizationActivity(context: Context) {
        try {
            logtoApiClient.discover { oidcConfiguration ->
                val codeChallenge = Pkce.generateCodeChallenge(codeVerifier)
                val intent = AuthorizationActivity.createHandleStartIntent(
                    context = context,
                    endpoint = generateAuthUrl(oidcConfiguration, codeChallenge),
                    redirectUri = logtoConfig.redirectUri,
                )
                context.startActivity(intent)
            }
        } catch (exception: LogtoException) {
            onComplete(exception, null)
        }
    }

    private fun ensureValidAuthorizationCode(data: Uri, redirectUri: String): String? {
        val authorizationCode = data.getQueryParameter(QueryKey.CODE)
        if (authorizationCode.isNullOrEmpty() ||
            !data.toString().startsWith(redirectUri)
        ) {
            return null
        }
        return authorizationCode
    }

    private fun generateAuthUrl(
        oidcConfiguration: OidcConfiguration,
        codeChallenge: String,
    ): String {
        val endpoint = oidcConfiguration.authorizationEndpoint
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
        return Utils.buildUriWithQueries(endpoint, queries).toString()
    }
}
