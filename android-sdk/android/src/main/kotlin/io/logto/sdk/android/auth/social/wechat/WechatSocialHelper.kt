package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

internal object WechatSocialHelper {
    // TODO - Load the appId from the global config
    private const val appId = "__YOUR_APP_ID__"

    fun getApi(context: Activity): IWXAPI = WXAPIFactory.createWXAPI(context, appId).apply {
        registerApp(appId)
    }
}
