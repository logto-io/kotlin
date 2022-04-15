package io.logto.sdk.android.auth.social

import android.app.Activity
import io.logto.sdk.android.auth.social.alipay.AlipaySocialSession
import io.logto.sdk.android.auth.social.web.WebSocialSession
import io.logto.sdk.android.auth.social.wechat.WechatSocialSession
import io.logto.sdk.android.completion.Completion

object SocialSessionHelper {
    fun createSocialSession(
        scheme: String,
        context: Activity,
        redirectTo: String,
        callbackUri: String,
        completion: Completion<String>,
    ): SocialSession? {
        return when (scheme) {
            "http", "https" -> WebSocialSession(context, redirectTo, callbackUri, completion)
            "alipay" -> AlipaySocialSession(context, redirectTo, callbackUri, completion)
            "wechat" -> WechatSocialSession(context, redirectTo, callbackUri, completion)
            else -> null
        }
    }
}
