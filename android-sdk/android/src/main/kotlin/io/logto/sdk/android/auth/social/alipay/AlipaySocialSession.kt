// Alipay SDK: https://opendocs.alipay.com/open/218/sxc60m
package io.logto.sdk.android.auth.social.alipay

import android.app.Activity
import android.net.Uri
import com.alipay.sdk.app.OpenAuthTask
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.core.util.GenerateUtils

class AlipaySocialSession(
    override val context: Activity,
    override val redirectTo: String,
    override val callbackUri: String,
    override val completion: Completion<String>,
) : SocialSession {
    companion object {
        private const val APP_ID = "app_id"
    }

    override fun start() {
        val appId = Uri.parse(redirectTo).getQueryParameter(APP_ID)
        if (appId.isNullOrBlank()) {
            completion.onComplete(LogtoException(LogtoException.Message.ALIPAY_APP_ID_NO_FOUND), null)
            return
        }

        val bizParams = HashMap<String, String>()
        bizParams["url"] = generateAlipayAuthUri(appId)
        val openAuthTask = OpenAuthTask(context)
        openAuthTask.execute(
            "${context.packageName}.logto-callback-alipay",
            OpenAuthTask.BizType.AccountAuth,
            bizParams,
            { resultCode, errorMessage, data ->
                if (resultCode != OpenAuthTask.OK) {
                    val logtoException =
                        LogtoException(LogtoException.Message.ALIPAY_AUTH_FAILED).apply {
                            detail = errorMessage
                        }
                    completion.onComplete(logtoException, null)
                    return@execute
                }

                /**
                 * Alipay SDK
                 * Auth Result: https://opendocs.alipay.com/open/218/105327#%E8%BF%94%E5%9B%9E%E7%BB%93%E6%9E%9C%E8%AF%B4%E6%98%8E
                 * Request params: https://opendocs.alipay.com/open/02ailc#%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0
                 * We only need "auth_code" as "code" parameter.
                 */
                // TODO - LOG-2186: Handle Errors in Social Sign in Process
                val authCode = data.getString("auth_code")
                val signInUri = Uri.parse(callbackUri).buildUpon().appendQueryParameter("code", authCode).build()
                completion.onComplete(null, signInUri.toString())
            },
            true,
        )
    }

    private fun generateAlipayAuthUri(appId: String): String {
        val authType = "PURE_OAUTH_SDK"
        val scope = "auth_user"
        val state = GenerateUtils.generateState()
        return "https://authweb.alipay.com/auth?auth_type=$authType&app_id=$appId&scope=$scope&state=$state"
    }
}
