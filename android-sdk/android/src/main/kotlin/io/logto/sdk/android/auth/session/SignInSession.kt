package io.logto.sdk.android.auth.session

import android.app.Activity
import io.logto.sdk.android.auth.activity.SignInActivity
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.Core
import io.logto.sdk.core.exception.CallbackUriVerificationException
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.util.CallbackUriUtils
import io.logto.sdk.core.util.GenerateUtils

class SignInSession(
    val context: Activity,
    val logtoConfig: LogtoConfig,
    val oidcConfig: OidcConfigResponse,
    val redirectUri: String,
    val completion: HttpCompletion<CodeTokenResponse>,
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
        val authorizationCode = try {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        } catch (exception: CallbackUriVerificationException) {
            completion.onComplete(exception, null)
            return
        }

        Core.fetchTokenByAuthorizationCode(
            tokenEndpoint = oidcConfig.tokenEndpoint,
            clientId = logtoConfig.clientId,
            redirectUri = redirectUri,
            codeVerifier = codeVerifier,
            code = authorizationCode,
            resource = null,
            completion::onComplete,
        )
    }

    fun handleUserCancel() {
        SignInSessionManager.clearSession()
        completion.onComplete(LogtoException(LogtoException.Message.USER_CANCELED), null)
    }
}
