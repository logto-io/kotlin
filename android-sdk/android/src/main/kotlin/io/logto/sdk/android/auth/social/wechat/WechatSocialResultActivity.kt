package io.logto.sdk.android.auth.social.wechat

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

open class WechatSocialResultActivity : Activity(), IWXAPIEventHandler {
    companion object {
        @SuppressLint("StaticFieldLeak")
        internal var wechatSocialSession: WechatSocialSession? = null
        fun registerSession(session: WechatSocialSession) {
            wechatSocialSession = session
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wechatSocialSession?.let {
            val redirectTo = it.redirectTo
            val appId = Uri.parse(redirectTo).getQueryParameter("app_id")
            if (appId.isNullOrBlank()) {
                it.handleMissingInformationError()
                wechatSocialSession = null
                finish()
                return
            }
            val api = WXAPIFactory.createWXAPI(this, appId).apply {
                registerApp(appId)
            }
            api.handleIntent(intent, this)
        } ?: finish()
    }

    override fun onReq(request: BaseReq?) {
        finish()
    }

    override fun onResp(response: BaseResp?) {
        wechatSocialSession?.handleResult(response)
        wechatSocialSession = null
        finish()
    }
}
