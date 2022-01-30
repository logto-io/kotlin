package io.logto.sdk.android

import io.logto.sdk.android.callback.RetrieveCallback
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.extension.oidcConfigEndpoint
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.util.LogtoUtils
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.HttpCompletion
import io.logto.sdk.core.type.IdTokenClaims
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.RefreshTokenTokenResponse
import io.logto.sdk.core.type.UserInfoResponse
import io.logto.sdk.core.util.TokenUtils
import org.jetbrains.annotations.TestOnly
import org.jose4j.jwt.consumer.InvalidJwtException

open class LogtoClient(
    val logtoConfig: LogtoConfig
) {
    protected val accessTokenMap: MutableMap<String, AccessToken> = mutableMapOf()

    protected var refreshToken: String? = null

    protected var idToken: String? = null

    protected var oidcConfig: OidcConfigResponse? = null

    val isAuthenticated
        get() = idToken != null

    fun getAccessToken(callback: RetrieveCallback<AccessToken>) =
        getAccessToken(null, null, callback)

    fun getAccessToken(
        resource: String?,
        scopes: List<String>?,
        getAccessTokenCallback: RetrieveCallback<AccessToken>,
    ) {
        if (!isAuthenticated) {
            getAccessTokenCallback.onResult(LogtoException(LogtoException.Message.NOT_AUTHENTICATED), null)
            return
        }

        resource?.let {
            if (logtoConfig.resources?.contains(it) == false) {
                getAccessTokenCallback.onResult(
                    LogtoException(LogtoException.Message.RESOURCE_IS_NOT_GRANTED).apply { detail = it }, null
                )
                return
            }
        }

        val finalScope = scopes ?: logtoConfig.scopes
        if (!logtoConfig.scopes.containsAll(finalScope)) {
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
            scopes = finalScope
        ) { throwable, response ->
            if (throwable != null) {
                getAccessTokenCallback.onResult(throwable, null)
                return@refreshToken
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

    private fun refreshToken(
        resource: String?,
        scopes: List<String>?,
        completion: HttpCompletion<RefreshTokenTokenResponse>,
    ) {
        if (refreshToken == null) {
            completion.onComplete(
                LogtoException(LogtoException.Message.MISSING_REFRESH_TOKEN), null
            )
            return
        }
        getOidcConfig { throwable, result ->
            if (throwable != null) {
                completion.onComplete(throwable, null)
                return@getOidcConfig
            }
            requireNotNull(result).let { oidcConfig ->
                Core.fetchTokenByRefreshToken(
                    tokenEndpoint = oidcConfig.tokenEndpoint,
                    clientId = logtoConfig.clientId,
                    refreshToken = requireNotNull(refreshToken),
                    resource = resource,
                    scopes = scopes,
                    completion = completion
                )
            }
        }
    }

    fun getIdTokenClaims(callback: RetrieveCallback<IdTokenClaims>) {
        if (!isAuthenticated) {
            callback.onResult(LogtoException(LogtoException.Message.NOT_AUTHENTICATED), null)
            return
        }
        try {
            val idTokenClaims = TokenUtils.decodeIdToken(requireNotNull(idToken))
            callback.onResult(null, idTokenClaims)
        } catch (exception: InvalidJwtException) {
            callback.onResult(exception, null)
        }
    }

    fun fetchUserInfo(callback: RetrieveCallback<UserInfoResponse>) {
        getOidcConfig { throwable, result ->
            throwable?.let {
                callback.onResult(it, null)
                return@getOidcConfig
            }
            requireNotNull(result).let { oidcConfig ->
                getAccessToken { throwable: Throwable?, result: AccessToken? ->
                    throwable?.let {
                        callback.onResult(it, null)
                        return@getAccessToken
                    }
                    Core.fetchUserInfo(oidcConfig.userinfoEndpoint, requireNotNull(result).token, callback::onResult)
                }
            }
        }
    }

    internal fun getOidcConfig(callback: RetrieveCallback<OidcConfigResponse>) {
        if (oidcConfig != null) {
            callback.onResult(null, oidcConfig)
            return
        }
        Core.fetchOidcConfig(
            logtoConfig.oidcConfigEndpoint
        ) { throwable, response ->
            oidcConfig = response
            callback.onResult(throwable, oidcConfig)
        }
    }

    internal fun buildAccessTokenKey(scopes: List<String>, resource: String?) =
        "${scopes.sorted().joinToString(" ")}@$resource"

    @TestOnly
    fun setupRefreshToken(token: String?) {
        refreshToken = token
    }

    @TestOnly
    fun setupIdToken(token: String?) {
        idToken = token
    }

    @TestOnly
    fun setupAccessTokenMap(tokenMap: Map<String, AccessToken>) {
        accessTokenMap.putAll(tokenMap)
    }
}
