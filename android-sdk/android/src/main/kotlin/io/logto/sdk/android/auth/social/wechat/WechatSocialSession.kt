// Wechat SDK: https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Access_Guide/Android.html
package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import android.net.Uri
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.logto.sdk.android.auth.social.SocialException
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.core.util.GenerateUtils

class WechatSocialSession(
    override val context: Activity,
    override val redirectTo: String,
    override val callbackUri: String,
    override val completion: Completion<SocialException, String>,
) : SocialSession {
    companion object {
        const val CONNECTOR_TARGET = "wechat"
        const val SDK_IDENTIFY_CLASS_NAME = "com.tencent.mm.opensdk.openapi.IWXAPI"
    }

    override fun start() {
        val appId = Uri.parse(redirectTo).getQueryParameter("app_id")
        if (appId.isNullOrBlank()) {
            handleMissingAppIdError()
            return
        }

        val api = WXAPIFactory.createWXAPI(context, appId).apply {
            registerApp(appId)
        }
        WechatSocialResultActivity.registerSession(this)
        val authRequest = SendAuth.Req().apply {
            scope = "snsapi_userinfo"
            state = GenerateUtils.generateState()
        }
        api.sendReq(authRequest)
    }

    fun handleResult(result: BaseResp?) {
        result?.takeIf { it.errCode == BaseResp.ErrCode.ERR_OK && it.type == ConstantsAPI.COMMAND_SENDAUTH }
            ?.let {
                /**
                 * Wechat SDK: https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html
                 * We only need "code" here
                 */
                val authResponse = it as SendAuth.Resp
                val continueSignInUri = try {
                    Uri.parse(callbackUri)
                        .buildUpon()
                        .appendQueryParameter("code", authResponse.code)
                        .build()
                } catch (_: UnsupportedOperationException) {
                    completion.onComplete(
                        SocialException(SocialException.Type.UNABLE_TO_CONSTRUCT_CALLBACK_URI),
                        null,
                    )
                    return
                }
                completion.onComplete(null, continueSignInUri.toString())
                return
            }

        val socialException = SocialException(SocialException.Type.AUTHENTICATION_FAILED).apply {
            socialCode = result?.errCode.toString()
            socialMessage = result?.errStr
        }
        completion.onComplete(socialException, null)
    }

    fun handleMissingAppIdError() =
        completion.onComplete(SocialException(SocialException.Type.INSUFFICIENT_INFORMATION), null)
}
