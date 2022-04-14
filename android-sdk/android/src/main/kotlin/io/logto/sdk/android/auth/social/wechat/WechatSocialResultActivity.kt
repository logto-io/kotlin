package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.logto.sdk.android.R

open class WechatSocialResultActivity : Activity(), IWXAPIEventHandler {
    companion object {
        private const val WECHAT_SOCIAL_SESSION_KEY = "wechat"
        private val wechatSocialSession = mutableMapOf<String, WechatSocialSession>()

        fun registerSession(session: WechatSocialSession) {
            wechatSocialSession[WECHAT_SOCIAL_SESSION_KEY] = session
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appId = this.resources.getString(R.string.wechat_app_id)
        if (appId.isBlank()) {
            wechatSocialSession
                .remove(WECHAT_SOCIAL_SESSION_KEY)
                ?.handleMissingAppIdError()
            finish()
        }
        val api = WXAPIFactory.createWXAPI(this, appId).apply {
            registerApp(appId)
        }
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
