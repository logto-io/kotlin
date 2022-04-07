package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.core.util.GenerateUtils

class WechatSocialSession(
    context: Activity,
    private val completion: Completion<String>,
) {
    private val api = WechatSocialHelper.getApi(context)

    fun start() {
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
}
