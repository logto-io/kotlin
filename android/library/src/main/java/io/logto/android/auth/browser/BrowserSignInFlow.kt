package io.logto.android.auth.browser

import android.content.Context
import android.net.Uri
import io.logto.android.auth.IFlow
import io.logto.android.auth.activity.AuthorizationActivity
import io.logto.android.client.LogtoAndroidClient
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.exception.LogtoException.Companion.EMPTY_REDIRECT_URI
import io.logto.client.exception.LogtoException.Companion.INVALID_REDIRECT_URI
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
        try {
            validateRedirectUri(redirectUri)
        } catch (exceptionOnValidate: LogtoException) {
            onComplete(exceptionOnValidate, null)
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

    @Suppress("ThrowsCount")
    private fun validateRedirectUri(uri: Uri) {
        if (uri.toString().isEmpty()) {
            throw LogtoException("$SIGN_IN_FAILED: $EMPTY_REDIRECT_URI")
        }

        val errorDescription = uri.getQueryParameter(QueryKey.ERROR_DESCRIPTION)
        if (errorDescription != null) {
            throw LogtoException("$SIGN_IN_FAILED: $errorDescription")
        }

        val error = uri.getQueryParameter(QueryKey.ERROR)
        if (error != null) {
            throw LogtoException("$SIGN_IN_FAILED: $error")
        }

        if (!uri.toString().startsWith(logtoAndroidClient.logtoConfig.redirectUri)) {
            throw LogtoException("$SIGN_IN_FAILED: $INVALID_REDIRECT_URI")
        }
    }
}
