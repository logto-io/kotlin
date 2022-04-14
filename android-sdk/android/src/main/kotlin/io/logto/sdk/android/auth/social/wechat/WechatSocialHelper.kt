package io.logto.sdk.android.auth.social.wechat

import android.app.Activity
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

internal object WechatSocialHelper {
    fun getApi(context: Activity, appId: String): IWXAPI =
        WXAPIFactory.createWXAPI(context, appId).apply {
            registerApp(appId)
        }
}
