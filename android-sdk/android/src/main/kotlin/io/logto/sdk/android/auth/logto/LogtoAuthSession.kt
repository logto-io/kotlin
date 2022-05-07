package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.Core
import io.logto.sdk.core.exception.CallbackUriVerificationException
import io.logto.sdk.core.type.CodeTokenResponse
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.util.CallbackUriUtils
import io.logto.sdk.core.util.GenerateUtils

class LogtoAuthSession(
    val context: Activity,
    val logtoConfig: LogtoConfig,
    val oidcConfig: OidcConfigResponse,
    val redirectUri: String,
    private val completion: Completion<LogtoException, CodeTokenResponse>,
) {
    private val codeVerifier = GenerateUtils.generateCodeVerifier()
    private val state = GenerateUtils.generateState()

    fun start() {
        if (Uri.parse(redirectUri) == Uri.EMPTY) {
            completion.onComplete(LogtoException(LogtoException.Type.INVALID_REDIRECT_URI), null)
            return
        }

        LogtoAuthManager.handleAuthStart(this)

        val signInUri = Core.generateSignInUri(
            authorizationEndpoint = oidcConfig.authorizationEndpoint,
            clientId = logtoConfig.appId,
            redirectUri = redirectUri,
            codeChallenge = GenerateUtils.generateCodeChallenge(codeVerifier),
            state = state,
            scopes = logtoConfig.scopes,
            resources = logtoConfig.resources,
        )

        LogtoWebViewAuthActivity.launch(context, signInUri)
    }

    fun handleCallbackUri(callbackUri: Uri) {
        val authorizationCode = try {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(
                callbackUri.toString(),
                redirectUri,
                state,
            )
        } catch (exception: CallbackUriVerificationException) {
            completion.onComplete(
                LogtoException(LogtoException.Type.INVALID_CALLBACK_URI, exception),
                null,
            )
            return
        }

        Core.fetchTokenByAuthorizationCode(
            tokenEndpoint = oidcConfig.tokenEndpoint,
            clientId = logtoConfig.appId,
            redirectUri = redirectUri,
            codeVerifier = codeVerifier,
            code = authorizationCode,
            resource = null,
        ) { fetchTokenException, codeTokenResponse ->
            fetchTokenException?.let {
                completion.onComplete(
                    LogtoException(
                        LogtoException.Type.UNABLE_TO_FETCH_TOKEN_BY_AUTHORIZATION_CODE,
                    ),
                    null,
                )
                return@fetchTokenByAuthorizationCode
            }
            completion.onComplete(null, codeTokenResponse)
        }
    }

    fun handleUserCancel() {
        completion.onComplete(LogtoException(LogtoException.Type.USER_CANCELED), null)
    }
}
