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
        private var wechatSocialSession: WechatSocialSession? = null
        fun registerSession(session: WechatSocialSession) {
            wechatSocialSession = session
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val redirectTo = wechatSocialSession?.redirectTo
        val appId = if (redirectTo == null) {
            // Note: Uri.parse will throw NullPointerException if redirectTo is null
            null
        } else {
            Uri.parse(redirectTo).getQueryParameter("app_id")
        }

        if (appId.isNullOrBlank()) {
            wechatSocialSession?.handleMissingAppIdError()
            wechatSocialSession = null
            finish()
            return
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
        wechatSocialSession?.handleResult(response)
        wechatSocialSession = null
        finish()
    }
}
