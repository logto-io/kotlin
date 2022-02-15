package io.logto.sdk.android

import android.app.Activity
import android.app.Application
import io.logto.sdk.android.auth.session.SignInSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.completion.EmptyCompletion
import io.logto.sdk.android.constant.StorageKey
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.extension.oidcConfigEndpoint
import io.logto.sdk.android.storage.PersistStorage
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.util.LogtoUtils.expiresAtFrom
import io.logto.sdk.android.util.LogtoUtils.nowRoundToSec
import io.logto.sdk.core.Core
import io.logto.sdk.core.http.httpGet
import io.logto.sdk.core.type.IdTokenClaims
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.UserInfoResponse
import io.logto.sdk.core.util.TokenUtils
import org.jetbrains.annotations.TestOnly
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.lang.JoseException

open class LogtoClient(
    val logtoConfig: LogtoConfig,
    application: Application,
) {
    protected val accessTokenMap: MutableMap<String, AccessToken> = mutableMapOf()

    protected var refreshToken: String? = null
        set(value) {
            storage?.setItem(StorageKey.REFRESH_TOKEN, value)
            field = value
        }

    protected var idToken: String? = null
        set(value) {
            storage?.setItem(StorageKey.ID_TOKEN, value)
            field = value
        }

    protected var oidcConfig: OidcConfigResponse? = null

    protected var jwks: JsonWebKeySet? = null

    val isAuthenticated
        get() = idToken != null

    private val storage = if (logtoConfig.usingPersistStorage) {
        PersistStorage(application, "${StorageKey.STORAGE_NAME_PREFIX}:${logtoConfig.clientId}")
    } else {
        null
    }

    init {
        loadFromStorage()
    }

    fun signInWithBrowser(
        context: Activity,
        redirectUri: String,
        completion: EmptyCompletion,
    ) {
        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it)
                return@getOidcConfig
            }

            val signInSession = SignInSession(
                context = context,
                logtoConfig = logtoConfig,
                oidcConfig = requireNotNull(oidcConfig),
                redirectUri = redirectUri,
            ) { fetchCodeTokenException, fetchedTokenResponse ->
                fetchCodeTokenException?.let {
                    completion.onComplete(
                        LogtoException(LogtoException.Message.UNABLE_TO_FETCH_TOKEN_BY_AUTHORIZATION_CODE, it)
                    )
                    return@SignInSession
                }

                val codeToken = requireNotNull(fetchedTokenResponse)
                // Note - Treat `resource` as `null`: https://github.com/logto-io/swift/pull/35#discussion_r795145645
                val accessTokenKey = buildAccessTokenKey(logtoConfig.scopes, null)
                val accessToken = AccessToken(
                    codeToken.accessToken,
                    codeToken.scope,
                    expiresAtFrom(nowRoundToSec(), codeToken.expiresIn)
                )

                verifyAndSaveTokenResponse(
                    issuer = oidcConfig.issuer,
                    responseIdToken = codeToken.idToken,
                    responseRefreshToken = codeToken.refreshToken,
                    accessTokenKey = accessTokenKey,
                    accessToken = accessToken,
                    completion = completion
                )
            }

            signInSession.start()
        }
    }

    fun signOut(completion: EmptyCompletion? = null) {
        if (!isAuthenticated) {
            completion?.onComplete(LogtoException(LogtoException.Message.NOT_AUTHENTICATED))
            return
        }

        accessTokenMap.clear()
        idToken = null

        refreshToken?.let { tokenToRevoke ->
            getOidcConfig { getOidcConfigException, oidcConfig ->
                getOidcConfigException?.let {
                    completion?.onComplete(it)
                    return@getOidcConfig
                }
                Core.revoke(
                    revocationEndpoint = requireNotNull(oidcConfig).revocationEndpoint,
                    clientId = logtoConfig.clientId,
                    token = tokenToRevoke
                ) { revokeException ->
                    completion?.onComplete(
                        revokeException?.let {
                            LogtoException(LogtoException.Message.UNABLE_TO_REVOKE_TOKEN, it)
                        }
                    )
                }
            }
        }

        refreshToken = null
    }

    fun getAccessToken(completion: Completion<AccessToken>) =
        getAccessToken(null, null, completion)

    fun getAccessToken(
        resource: String?,
        scopes: List<String>?,
        completion: Completion<AccessToken>,
    ) {
        if (!isAuthenticated) {
            completion.onComplete(LogtoException(LogtoException.Message.NOT_AUTHENTICATED), null)
            return
        }

        resource?.let {
            if (logtoConfig.resources?.contains(it) == false) {
                completion.onComplete(
                    LogtoException(LogtoException.Message.UNGRANTED_RESOURCE_FOUND).apply { detail = it }, null
                )
                return
            }
        }

        val finalScope = scopes ?: logtoConfig.scopes
        if (!logtoConfig.scopes.containsAll(finalScope)) {
            completion.onComplete(
                LogtoException(LogtoException.Message.UNGRANTED_SCOPE_FOUND).apply {
                    detail = finalScope.toString()
                },
                null
            )
            return
        }

        // MARK: Retrieve access token from accessTokenMap
        val accessTokenKey = buildAccessTokenKey(finalScope, resource)
        val accessToken = accessTokenMap[accessTokenKey]
        accessToken?.let {
            if (it.expiresAt > nowRoundToSec()) {
                completion.onComplete(null, it)
                return
            }
        }

        // MARK: If no access token is valid, fetch a new token by refresh token
        if (refreshToken == null) {
            completion.onComplete(LogtoException(LogtoException.Message.NO_REFRESH_TOKEN_FOUND), null)
            return
        }

        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it, null)
                return@getOidcConfig
            }

            Core.fetchTokenByRefreshToken(
                tokenEndpoint = requireNotNull(oidcConfig).tokenEndpoint,
                clientId = logtoConfig.clientId,
                refreshToken = requireNotNull(refreshToken),
                resource = resource,
                scopes = scopes,
            ) { fetchRefreshedTokenException, fetchedTokenResponse ->
                fetchRefreshedTokenException?.let {
                    completion.onComplete(
                        LogtoException(
                            LogtoException.Message.UNABLE_TO_FETCH_TOKEN_BY_REFRESH_TOKEN,
                            it
                        ),
                        null
                    )
                    return@fetchTokenByRefreshToken
                }

                val refreshedToken = requireNotNull(fetchedTokenResponse)
                val refreshedAccessToken = AccessToken(
                    token = refreshedToken.accessToken,
                    scope = refreshedToken.scope,
                    expiresAt = expiresAtFrom(
                        nowRoundToSec(),
                        refreshedToken.expiresIn
                    )
                )

                verifyAndSaveTokenResponse(
                    issuer = oidcConfig.issuer,
                    responseIdToken = refreshedToken.idToken,
                    responseRefreshToken = refreshedToken.refreshToken,
                    accessTokenKey = accessTokenKey,
                    accessToken = refreshedAccessToken
                ) { verifyException ->
                    verifyException?.let { completion.onComplete(it, null) }
                        ?: completion.onComplete(null, refreshedAccessToken)
                }
            }
        }
    }

    fun getIdTokenClaims(completion: Completion<IdTokenClaims>) {
        if (!isAuthenticated) {
            completion.onComplete(LogtoException(LogtoException.Message.NOT_AUTHENTICATED), null)
            return
        }
        try {
            val idTokenClaims = TokenUtils.decodeIdToken(requireNotNull(idToken))
            completion.onComplete(null, idTokenClaims)
        } catch (exception: InvalidJwtException) {
            completion.onComplete(
                LogtoException(LogtoException.Message.UNABLE_TO_PARSE_ID_TOKEN_CLAIMS, exception),
                null
            )
        }
    }

    fun fetchUserInfo(completion: Completion<UserInfoResponse>) {
        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it, null)
                return@getOidcConfig
            }
            getAccessToken { getAccessTokenException, accessToken ->
                getAccessTokenException?.let {
                    completion.onComplete(it, null)
                    return@getAccessToken
                }
                Core.fetchUserInfo(
                    userInfoEndpoint = requireNotNull(oidcConfig).userinfoEndpoint,
                    accessToken = requireNotNull(accessToken).token,
                ) fetchUserInfoInCore@{ fetchUserInfoException, userInfoResponse ->
                    fetchUserInfoException?.let {
                        completion.onComplete(
                            LogtoException(LogtoException.Message.UNABLE_TO_FETCH_USER_INFO, it),
                            null
                        )
                        return@fetchUserInfoInCore
                    }
                    completion.onComplete(null, userInfoResponse)
                }
            }
        }
    }

    @Suppress("LongParameterList")
    private fun verifyAndSaveTokenResponse(
        issuer: String,
        responseIdToken: String?,
        responseRefreshToken: String,
        accessTokenKey: String,
        accessToken: AccessToken,
        completion: EmptyCompletion,
    ) {
        getJwks { getJwksException, jwks ->
            getJwksException?.let {
                completion.onComplete(it)
                return@getJwks
            }
            responseIdToken?.let {
                try {
                    TokenUtils.verifyIdToken(it, logtoConfig.clientId, issuer, requireNotNull(jwks))
                } catch (exception: InvalidJwtException) {
                    completion.onComplete(LogtoException(LogtoException.Message.INVALID_ID_TOKEN, exception))
                    return@getJwks
                }
                idToken = it
            }

            accessTokenMap[accessTokenKey] = accessToken
            refreshToken = responseRefreshToken
            completion.onComplete(null)
        }
    }

    internal fun getOidcConfig(completion: Completion<OidcConfigResponse>) {
        if (oidcConfig != null) {
            completion.onComplete(null, oidcConfig)
            return
        }
        Core.fetchOidcConfig(
            logtoConfig.oidcConfigEndpoint
        ) { fetchOidcConfigException, oidcConfigResponse ->
            fetchOidcConfigException?.let {
                completion.onComplete(LogtoException(LogtoException.Message.UNABLE_TO_FETCH_OIDC_CONFIG, it), null)
                return@fetchOidcConfig
            }
            oidcConfig = oidcConfigResponse
            completion.onComplete(null, oidcConfig)
        }
    }

    internal fun getJwks(completion: Completion<JsonWebKeySet>) {
        jwks?.let {
            completion.onComplete(null, it)
            return
        }

        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it, null)
                return@getOidcConfig
            }

            httpGet<String>(requireNotNull(oidcConfig).jwksUri) { fetchJwksJsonException, jwksJson ->
                fetchJwksJsonException?.let {
                    completion.onComplete(LogtoException(LogtoException.Message.UNABLE_TO_FETCH_JWKS_JSON, it), null)
                    return@httpGet
                }

                try {
                    jwks = JsonWebKeySet(jwksJson)
                } catch (joseException: JoseException) {
                    completion.onComplete(
                        LogtoException(LogtoException.Message.UNABLE_TO_PARSE_JWKS, joseException),
                        null
                    )
                    return@httpGet
                }

                completion.onComplete(null, jwks)
            }
        }
    }

    private fun loadFromStorage() {
        refreshToken = storage?.getItem(StorageKey.REFRESH_TOKEN)
        idToken = storage?.getItem(StorageKey.ID_TOKEN)
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
