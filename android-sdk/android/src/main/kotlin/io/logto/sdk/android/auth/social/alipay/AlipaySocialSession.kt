package io.logto.sdk.android.auth.social.alipay

import android.app.Activity
import com.alipay.sdk.app.OpenAuthTask
import io.logto.sdk.android.R
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.core.util.GenerateUtils

class AlipaySocialSession(
    private val context: Activity,
    private val completion: Completion<String>,
) {
    fun start() {
        val appId = context.resources.getString(R.string.alipay_app_id)
        if (appId.isBlank()) {
            completion.onComplete(LogtoException(LogtoException.Message.ALIPAY_APP_ID_NO_FOUND), null)
            return
        }
        val bizParams = HashMap<String, String>()
        bizParams["url"] = generateAlipayAuthUri(appId)
        val openAuthTask = OpenAuthTask(context)
        openAuthTask.execute(
            "${context.packageName}.logto-alipay-callback",
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
