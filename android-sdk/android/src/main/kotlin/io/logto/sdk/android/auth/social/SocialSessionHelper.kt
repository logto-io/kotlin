package io.logto.sdk.android.auth.social

import android.app.Activity
import android.net.Uri
import io.logto.sdk.android.auth.social.alipay.AlipaySocialSession
import io.logto.sdk.android.auth.social.web.WebSocialSession
import io.logto.sdk.android.auth.social.wechat.WechatSocialSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.util.LogtoUtils.isDependencyInstalled

object SocialSessionHelper {
    fun createSocialSession(
        context: Activity,
        redirectTo: String,
        callbackUri: String,
        completion: Completion<SocialException, String>,
    ): SocialSession? {
        return when (Uri.parse(redirectTo).scheme) {
            "http", "https" -> WebSocialSession(context, redirectTo, callbackUri, completion)
            "alipay" -> AlipaySocialSession(context, redirectTo, callbackUri, completion)
            "wechat" -> WechatSocialSession(context, redirectTo, callbackUri, completion)
            else -> null
        }
    }

    private val nativeSocialSdkIdentifyMeta = mapOf(
        AlipaySocialSession.CONNECTOR_TARGET to AlipaySocialSession.SDK_IDENTIFY_CLASS_NAME,
        WechatSocialSession.CONNECTOR_TARGET to WechatSocialSession.SDK_IDENTIFY_CLASS_NAME,
    )

    fun getSupportedSocialConnectorTargets() =
        nativeSocialSdkIdentifyMeta
            .filter { (_, sdkIdentifyClassName) -> isDependencyInstalled(sdkIdentifyClassName) }
            .map { (connectorTarget) -> connectorTarget }
}
