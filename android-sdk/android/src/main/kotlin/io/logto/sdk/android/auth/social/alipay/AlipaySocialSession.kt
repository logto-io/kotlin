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
                completion.onComplete(null, data.getString("auth_code"))
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
