package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.MISSING_AUTHORIZATION_CODE
import io.logto.client.exception.LogtoException.Companion.SIGN_IN_FAILED
import io.logto.client.model.TokenSet
import io.logto.client.utils.PkceUtils

class BrowserSignInFlow(
    private val logtoAndroidClient: LogtoAndroidClient,
    private val onComplete: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
) : IFlow {
    private val codeVerifier: String = PkceUtils.generateCodeVerifier()

    override fun start(context: Context) {
        try {
            logtoAndroidClient.getOidcConfigurationAsync { oidcConfiguration ->
                val codeChallenge = PkceUtils.generateCodeChallenge(codeVerifier)
                val intent = AuthorizationActivity.createHandleStartIntent(
                    context = context,
                    endpoint = logtoAndroidClient.getSignInUrl(
                        oidcConfiguration.authorizationEndpoint,
                        codeChallenge
                    ),
                    redirectUri = logtoAndroidClient.logtoConfig.redirectUri,
                )
                context.startActivity(intent)
            }
        } catch (exception: LogtoException) {
            onComplete(exception, null)
        }
    }

    override fun handleRedirectUri(redirectUri: Uri) {
        val exceptionMsg = Utils.validateRedirectUri(
            redirectUri,
            logtoAndroidClient.logtoConfig.redirectUri,
        )
        if (exceptionMsg != null) {
            onComplete(LogtoException("$SIGN_IN_FAILED: $exceptionMsg"), null)
            return
        }

        val authorizationCode = redirectUri.getQueryParameter(QueryKey.CODE)
        if (authorizationCode.isNullOrEmpty()) {
            onComplete(LogtoException("$SIGN_IN_FAILED: $MISSING_AUTHORIZATION_CODE"), null)
            return
        }

        try {
            logtoAndroidClient.grantTokenByAuthorizationCodeAsync(
                authorizationCode = authorizationCode,
                codeVerifier = codeVerifier,
            ) {
                onComplete(null, it)
            }
        } catch (exception: LogtoException) {
            onComplete(exception, null)
        }
    }
}
