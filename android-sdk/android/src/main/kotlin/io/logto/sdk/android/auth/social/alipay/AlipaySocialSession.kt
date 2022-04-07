package io.logto.sdk.android.auth.social.alipay

import android.app.Activity
import com.alipay.sdk.app.OpenAuthTask
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.core.util.GenerateUtils

class AlipaySocialSession(
    private val context: Activity,
    private val completion: Completion<String>
) {
    companion object {
        // TODO - Use an app-based scheme
        private const val ALIPAY_CALLBACK_SCHEME = "logto_social_alipay"
    }
    fun start() {
        val bizParams = HashMap<String, String>()
        bizParams["url"] = generateAlipayAuthUri()
        val openAuthTask = OpenAuthTask(context)
        openAuthTask.execute(
            ALIPAY_CALLBACK_SCHEME,
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
            true
        )
    }

    private fun generateAlipayAuthUri(): String {
        // TODO - Load the appId from the global config
        val appId = "__YOUR_APP_ID__"
        val authType = "PURE_OAUTH_SDK"
        val scope = "auth_user"
        val state = GenerateUtils.generateState()
        return "https://authweb.alipay.com/auth?auth_type=$authType&app_id=$appId&scope=$scope&state=$state"
    }
}
