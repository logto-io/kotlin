package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.auth.social.SocialSessionHelper
import org.json.JSONException
import org.json.JSONObject

class LogtoWebViewSocialHandler(
    private val webView: WebView,
    private val hostActivity: Activity,
) {
    companion object {
        const val NAME = "SocialHandler"

        private object DataKey {
            const val REDIRECT_TO = "redirectTo"
            const val CALLBACK_URI = "callbackUri"
        }
    }

    fun getInjectSocialScript() = """
        window.logtoNativeSdk = {
            platform: 'android',
            getPostMessage: () => (data) => window.$NAME.postMessage(JSON.stringify(data)),
            supportedSocialConnectorIds: [${getSupportedSocialConnectorIds()}],
            callbackUriScheme: 'logto-callback://${hostActivity.packageName}/web',
        };
    """.trimIndent()

    private fun getSupportedSocialConnectorIds() = SocialSessionHelper
        .getSupportedSocialConnectorIds()
        .joinToString(", ") { "'$it'" }

    @JavascriptInterface
    fun postMessage(jsonData: String) {
        val data = try {
            JSONObject(jsonData)
        } catch (_: JSONException) {
            val socialException = SocialException(SocialException.Type.INVALID_JSON)
            postSocialException(socialException)
            return
        }

        val redirectTo = data.getString(DataKey.REDIRECT_TO)
        val callbackUri = data.getString(DataKey.CALLBACK_URI)

        val scheme = Uri.parse(redirectTo).scheme
        if (scheme.isNullOrBlank()) {
            postSocialException(SocialException(SocialException.Type.INVALID_REDIRECT_TO))
            return
        }

        if (Uri.parse(callbackUri) == Uri.EMPTY) {
            postSocialException(SocialException(SocialException.Type.INVALID_CALLBACK_URI))
            return
        }

        webView.post {
            val socialSession = SocialSessionHelper.createSocialSession(
                context = hostActivity,
                redirectTo = redirectTo,
                callbackUri = callbackUri,
            ) { exception, continueSignInUri ->
                exception?.let {
                    postSocialException(it)
                } ?: webView.loadUrl(requireNotNull(continueSignInUri))
            }

            socialSession?.start() ?: postSocialException(
                SocialException(SocialException.Type.UNKNOWN_SOCIAL_SCHEME),
            )
        }
    }

    internal fun postSocialException(exception: SocialException) {
        val script = """
            window.postMessage({
                type: 'error',
                code: '${exception.code}',
                ${exception.socialCode?.let { "socialCode: '$it'," } ?: ""}
                ${exception.socialMessage?.let { "socialMessage: '$it'," } ?: ""}
            });
        """.trimIndent()
            // Remove all empty lines
            .replace(Regex("(?m)^[ \t]*\r?\n"), "")

        webView.evaluateJavascript(script, null)
    }
}
