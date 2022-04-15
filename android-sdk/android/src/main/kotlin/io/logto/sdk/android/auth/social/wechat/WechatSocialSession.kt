package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import android.net.Uri
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.logto.sdk.android.auth.social.SocialSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.core.util.GenerateUtils

class WechatSocialSession(
    override val context: Activity,
    override val redirectTo: String,
    override val callbackUri: String,
    override val completion: Completion<String>,
) : SocialSession {
    companion object {
        const val APP_ID = "app_id"
    }

    override fun start() {
        val appId = Uri.parse(redirectTo).getQueryParameter(APP_ID)
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
        result?.let {
            if (it.errCode == BaseResp.ErrCode.ERR_OK) {
                if (it.type == ConstantsAPI.COMMAND_SENDAUTH) {
                    completion.onComplete(null, (it as SendAuth.Resp).code)
                    return
                }
            }
        }
        completion.onComplete(LogtoException(LogtoException.Message.WECHAT_AUTH_FAILED), null)
    }

    fun handleMissingAppIdError() =
        completion.onComplete(LogtoException(LogtoException.Message.WECHAT_APP_ID_NO_FOUND), null)
}
