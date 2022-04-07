package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

open class WechatSocialResultActivity : Activity(), IWXAPIEventHandler {
    companion object {
        private const val WECHAT_SOCIAL_SESSION_KEY = "wechat"
        private val wechatSocialSession = mutableMapOf<String, WechatSocialSession>()

        fun registerSession(session: WechatSocialSession) {
            wechatSocialSession[WECHAT_SOCIAL_SESSION_KEY] = session
        }
    }

    private val api by lazy {
        WechatSocialHelper.getApi(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api.handleIntent(intent, this)
    }

    override fun onReq(request: BaseReq?) {
        finish()
    }

    override fun onResp(response: BaseResp?) {
        wechatSocialSession
            .remove(WECHAT_SOCIAL_SESSION_KEY)
            ?.handleResult(response)
        finish()
    }
}
