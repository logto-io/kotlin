package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.callback.HandleTokenSetCallback
import io.logto.android.client.LogtoAndroidClient
import io.logto.android.utils.Utils
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.MISSING_AUTHORIZATION_CODE
import io.logto.client.exception.LogtoException.Companion.MISSING_STATE
import io.logto.client.exception.LogtoException.Companion.SIGN_IN_FAILED
import io.logto.client.exception.LogtoException.Companion.UNKNOWN_STATE
import io.logto.client.exception.LogtoException.Companion.USER_CANCELED
import io.logto.client.utils.GenerateUtils

class BrowserSignInFlow(
    private val logtoAndroidClient: LogtoAndroidClient,
    private val onComplete: HandleTokenSetCallback
) : IFlow {
    private val codeVerifier: String = GenerateUtils.generateCodeVerifier()
    private val state: String = GenerateUtils.generateState()

    override fun start(context: Context) {
        logtoAndroidClient.getOidcConfigurationAsync { exception, oidcConfiguration ->
            if (exception != null) {
                onComplete(exception, null)
                return@getOidcConfigurationAsync
            }
            val codeChallenge = GenerateUtils.generateCodeChallenge(codeVerifier)
            val intent = AuthorizationActivity.createHandleStartIntent(
                context = context,
                endpoint = logtoAndroidClient.getSignInUrl(
                    oidcConfiguration!!.authorizationEndpoint,
                    codeChallenge,
                    state,
                ),
                redirectUri = logtoAndroidClient.logtoConfig.redirectUri,
            )
            context.startActivity(intent)
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

        val state = redirectUri.getQueryParameter(QueryKey.STATE)
        if (state.isNullOrBlank()) {
            onComplete(LogtoException("$SIGN_IN_FAILED: $MISSING_STATE"), null)
            return
        }
        if (state != this.state) {
            onComplete(LogtoException("$SIGN_IN_FAILED: $UNKNOWN_STATE"), null)
            return
        }

        val authorizationCode = redirectUri.getQueryParameter(QueryKey.CODE)
        if (authorizationCode.isNullOrBlank()) {
            onComplete(LogtoException("$SIGN_IN_FAILED: $MISSING_AUTHORIZATION_CODE"), null)
            return
        }

        logtoAndroidClient.grantTokenByAuthorizationCodeAsync(
            authorizationCode = authorizationCode,
            codeVerifier = codeVerifier,
        ) { exception, tokenSet ->
            onComplete(exception, tokenSet)
        }
    }

    override fun handleUserCanceled() {
        onComplete(LogtoException(USER_CANCELED), null)
    }
}
