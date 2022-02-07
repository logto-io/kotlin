package io.logto.sdk.android.auth.session

import android.app.Activity
import io.logto.sdk.android.auth.activity.SignInActivity
import io.logto.sdk.android.callback.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.Core
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.util.GenerateUtils

class SignInSession(
    val context: Activity,
    val logtoConfig: LogtoConfig,
    val oidcConfig: OidcConfigResponse,
    val redirectUri: String,
    val completion: Completion<String>,
) {
    private val codeVerifier = GenerateUtils.generateCodeVerifier()
    private val state = GenerateUtils.generateState()

    fun start() {
        SignInSessionManager.setSession(this)
        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = oidcConfig.authorizationEndpoint,
            clientId = logtoConfig.clientId,
            redirectUri = redirectUri,
            codeChallenge = GenerateUtils.generateCodeChallenge(codeVerifier),
            state = state,
            scopes = logtoConfig.scopes,
            resources = logtoConfig.resources
        )
        val intent = SignInActivity.createIntent(context, signInUri, redirectUri)
        context.startActivity(intent)
    }

    fun handleCallbackUri(callbackUri: String) {
        // TODO - fetch token by authorization code
        completion.onComplete(null, callbackUri)
    }

    fun handleUserCancel() {
        SignInSessionManager.clearSession()
        completion.onComplete(LogtoException(LogtoException.Message.USER_CANCELED), null)
    }
}
