package io.logto.sdk.android

import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.type.OidcConfigResponse

open class LogtoClient(
    val logtoConfig: LogtoConfig
) {
    protected val accessTokenMap: Map<String, AccessToken> = mutableMapOf()

    protected var refreshToken: String? = null

    protected var idToken: String? = null

    protected var oidcConfig: OidcConfigResponse? = null

    fun isAuthenticated() = idToken != null
}
