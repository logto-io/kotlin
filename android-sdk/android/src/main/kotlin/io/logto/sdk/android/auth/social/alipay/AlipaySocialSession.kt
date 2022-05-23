// Alipay SDK: https://opendocs.alipay.com/open/218/sxc60m
package io.logto.sdk.android.auth.social.alipay

import android.app.Activity
import android.net.Uri
import com.alipay.sdk.app.OpenAuthTask
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.completion.Completion

class AlipaySocialSession(
    override val context: Activity,
    override val redirectTo: String,
    override val callbackUri: String,
    override val completion: Completion<SocialException, String>,
) : SocialSession {
    companion object {
        const val CONNECTOR_TARGET = "alipay"
        const val SDK_IDENTIFY_CLASS_NAME = "com.alipay.sdk.app.OpenAuthTask"
    }

    override fun start() {
        val parsedUri = Uri.parse(redirectTo)
        val appId = parsedUri.getQueryParameter("app_id")
        val state = parsedUri.getQueryParameter("state")

        if (appId.isNullOrBlank() or state.isNullOrBlank()) {
            completion.onComplete(
                SocialException(SocialException.Type.INSUFFICIENT_INFORMATION),
                null,
            )
            return
        }

        val bizParams = HashMap<String, String>()
        bizParams["url"] = generateAlipayAuthUri(requireNotNull(appId), requireNotNull(state))
        val openAuthTask = OpenAuthTask(context)
        openAuthTask.execute(
            "logto-callback://${context.packageName}/alipay",
            OpenAuthTask.BizType.AccountAuth,
            bizParams,
            { resultCode, errorMessage, data ->
                if (resultCode != OpenAuthTask.OK) {
                    val socialException =
                        SocialException(SocialException.Type.AUTHENTICATION_FAILED).apply {
                            socialCode = resultCode.toString()
                            socialMessage = errorMessage
                        }
                    completion.onComplete(socialException, null)
                    return@execute
                }

                /**
                 * Alipay SDK
                 * Auth Result: https://opendocs.alipay.com/open/218/105327#%E8%BF%94%E5%9B%9E%E7%BB%93%E6%9E%9C%E8%AF%B4%E6%98%8E
                 * Request params: https://opendocs.alipay.com/open/02ailc#%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0
                 * We only need "auth_code" as "code" parameter.
                 */
                val authCode = data.getString("auth_code")
                val continueSignInUri = try {
                    Uri.parse(callbackUri)
                        .buildUpon()
                        .appendQueryParameter("code", authCode)
                        .appendQueryParameter("state", state)
                        .build()
                } catch (_: UnsupportedOperationException) {
                    completion.onComplete(
                        SocialException(SocialException.Type.UNABLE_TO_CONSTRUCT_CALLBACK_URI),
                        null,
                    )
                    return@execute
                }
                completion.onComplete(null, continueSignInUri.toString())
            },
            true,
        )
    }

    private fun generateAlipayAuthUri(appId: String, state: String): String {
        val authType = "PURE_OAUTH_SDK"
        val scope = "auth_user"
        return "https://authweb.alipay.com/auth?auth_type=$authType&app_id=$appId&scope=$scope&state=$state"
    }
}
