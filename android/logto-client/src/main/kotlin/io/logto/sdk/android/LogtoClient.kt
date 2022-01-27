package io.logto.sdk.android

import io.logto.sdk.android.callback.RetrieveCallback
import io.logto.sdk.android.extension.oidcConfigEndpoint
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.OidcConfigResponse

open class LogtoClient(
    val logtoConfig: LogtoConfig
) {
    protected val accessTokenMap: Map<String, AccessToken> = mutableMapOf()

    protected var refreshToken: String? = null

    protected var idToken: String? = null

    protected var oidcConfig: OidcConfigResponse? = null

    fun isAuthenticated() = idToken != null

    internal fun getOidcConfig(callback: RetrieveCallback<OidcConfigResponse>) {
        if (oidcConfig != null) {
            callback.onResult(null, oidcConfig)
            return
        }
        Core.fetchOidcConfig(
            logtoConfig.oidcConfigEndpoint,
            object : HttpCompletion<OidcConfigResponse> {
                override fun onComplete(throwable: Throwable?, result: OidcConfigResponse?) {
                    oidcConfig = result
                    callback.onResult(throwable, oidcConfig)
                }
            }
        )
    }
}
