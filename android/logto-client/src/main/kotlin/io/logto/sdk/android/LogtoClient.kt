package io.logto.sdk.android

import io.logto.sdk.android.callback.RetrieveCallback
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.extension.oidcConfigEndpoint
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.util.LogtoUtils
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import org.jetbrains.annotations.TestOnly

open class LogtoClient(
    val logtoConfig: LogtoConfig
) {
    protected val accessTokenMap: MutableMap<String, AccessToken> = mutableMapOf()

    protected var refreshToken: String? = null

    protected var idToken: String? = null

    protected var oidcConfig: OidcConfigResponse? = null

    fun isAuthenticated() = idToken != null

    fun getAccessToken(callback: RetrieveCallback<AccessToken>) =
        getAccessToken(null, null, callback)

    fun getAccessToken(
        resource: String?,
        scope: List<String>?,
        getAccessTokenCallback: RetrieveCallback<AccessToken>,
    ) {
        if (!isAuthenticated()) {
            getAccessTokenCallback.onResult(LogtoException(LogtoException.Message.NOT_AUTHENTICATED), null)
            return
        }

        resource?.let {
            if (logtoConfig.resource?.contains(it) == false) {
                getAccessTokenCallback.onResult(
                    LogtoException(LogtoException.Message.RESOURCE_IS_NOT_GRANTED).apply { detail = it }, null
                )
                return
            }
        }

        val finalScope = scope ?: logtoConfig.scope
        if (!logtoConfig.scope.containsAll(finalScope)) {
            getAccessTokenCallback.onResult(
                LogtoException(LogtoException.Message.SCOPES_ARE_NOT_ALL_GRANTED).apply {
                    detail = finalScope.toString()
                },
                null
            )
            return
        }

        // Retrieve access token from accessTokenMap
        val accessTokenKey = buildAccessTokenKey(finalScope, resource)
        val accessToken = accessTokenMap[accessTokenKey]
        accessToken?.let {
            if (it.expiresAt > LogtoUtils.nowRoundToSec()) {
                getAccessTokenCallback.onResult(null, it)
                return
            }
        }

        // If no access token is valid, fetch a new token by refresh token
        refreshToken(
            resource = resource,
            scope = finalScope,
            refreshTokenCompletion = object : HttpCompletion<RefreshTokenTokenResponse> {
                override fun onComplete(throwable: Throwable?, response: RefreshTokenTokenResponse?) {
                    if (throwable != null) {
                        getAccessTokenCallback.onResult(throwable, null)
                        return
                    }
                    requireNotNull(response).let { tokenResponse ->
                        val refreshedAccessToken = AccessToken(
                            token = tokenResponse.accessToken,
                            scope = tokenResponse.scope,
                            expiresAt = LogtoUtils.expiresAtFrom(
                                LogtoUtils.nowRoundToSec(),
                                tokenResponse.expiresIn
                            )
                        )
                        accessTokenMap[accessTokenKey] = refreshedAccessToken
                        refreshToken = tokenResponse.refreshToken
                        tokenResponse.idToken?.let { idToken = it }
                        getAccessTokenCallback.onResult(null, refreshedAccessToken)
                    }
                }
            }
        )
    }

    private fun refreshToken(
        resource: String?,
        scope: List<String>?,
        refreshTokenCompletion: HttpCompletion<RefreshTokenTokenResponse>,
    ) {
        if (refreshToken == null) {
            refreshTokenCompletion.onComplete(
                LogtoException(LogtoException.Message.MISSING_REFRESH_TOKEN), null
            )
            return
        }
        getOidcConfig(object : RetrieveCallback<OidcConfigResponse> {
            override fun onResult(throwable: Throwable?, result: OidcConfigResponse?) {
                if (throwable != null) {
                    refreshTokenCompletion.onComplete(throwable, null)
                    return
                }
                requireNotNull(result).let { oidcConfig ->
                    Core.fetchTokenByRefreshToken(
                        tokenEndpoint = oidcConfig.tokenEndpoint,
                        clientId = logtoConfig.clientId,
                        refreshToken = requireNotNull(refreshToken),
                        resource = resource,
                        scope = scope,
                        completion = refreshTokenCompletion
                    )
                }
            }
        })
    }

    internal fun getOidcConfig(callback: RetrieveCallback<OidcConfigResponse>) {
        if (oidcConfig != null) {
            callback.onResult(null, oidcConfig)
            return
        }
        Core.fetchOidcConfig(
            logtoConfig.oidcConfigEndpoint,
            object : HttpCompletion<OidcConfigResponse> {
                override fun onComplete(throwable: Throwable?, response: OidcConfigResponse?) {
                    oidcConfig = response
                    callback.onResult(throwable, oidcConfig)
                }
            }
        )
    }

    internal fun buildAccessTokenKey(scope: List<String>, resource: String?) =
        "${scope.sorted().joinToString(" ")}@$resource"

    @TestOnly
    fun setupRefreshToken(token: String?) {
        refreshToken = token
    }

    @TestOnly
    fun setupAccessTokenMap(tokenMap: Map<String, AccessToken>) {
        accessTokenMap.putAll(tokenMap)
    }
}
