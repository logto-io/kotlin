package io.logto.sdk.android

import android.app.Activity
import android.app.Application
import android.webkit.CookieManager
import io.logto.sdk.android.auth.logto.LogtoAuthSession
import io.logto.sdk.android.completion.Completion
import io.logto.sdk.android.completion.EmptyCompletion
import io.logto.sdk.android.constant.StorageKey
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.extension.oidcConfigEndpoint
import io.logto.sdk.android.storage.PersistStorage
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.android.type.SignInOptions
import io.logto.sdk.android.util.LogtoUtils.expiresAtFrom
import io.logto.sdk.android.util.LogtoUtils.nowRoundToSec
import io.logto.sdk.core.Core
import io.logto.sdk.core.constant.UserScope
import io.logto.sdk.core.type.IdTokenClaims
import io.logto.sdk.core.type.OidcConfigResponse
import io.logto.sdk.core.type.UserInfoResponse
import io.logto.sdk.core.util.TokenUtils
import org.jetbrains.annotations.TestOnly
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.lang.JoseException

open class LogtoClient(
    val logtoConfig: LogtoConfig,
    application: Application,
) {
    /**
     * Cached access tokens.
     */
    protected val accessTokenMap: MutableMap<String, AccessToken> = mutableMapOf()

    /**
     * The cached refresh token.
     */
    protected var refreshToken: String? = null
        set(value) {
            storage?.setItem(StorageKey.REFRESH_TOKEN, value)
            field = value
        }

    /**
     * The cached ID Token in raw string format.
     * Use [getIdTokenClaims] to retrieve the claims of the ID Token.
     */
    protected var idToken: String? = null
        set(value) {
            storage?.setItem(StorageKey.ID_TOKEN, value)
            field = value
        }

    /**
     * The cached oidc config fetched from the OIDC Discovery endpoint.
     */
    protected var oidcConfig: OidcConfigResponse? = null

    /**
     * The cached JSON Web Key Set fetched from the jwks_uri endpoint.
     */
    protected var jwks: JsonWebKeySet? = null

    /**
     * Whether the user has been authenticated.
     */
    val isAuthenticated
        get() = idToken != null

    private val storage = if (logtoConfig.usingPersistStorage) {
        PersistStorage(application, "${StorageKey.STORAGE_NAME_PREFIX}:${logtoConfig.appId}")
    } else {
        null
    }

    init {
        loadFromStorage()
    }

    /**
     * Sign in
     * @param[context] the activity to perform a sign-in action
     * @param[options] the sign-in options
     * @param[completion] the completion which handles the result of signing in
     */
    fun signIn(
        context: Activity,
        options: SignInOptions,
        completion: EmptyCompletion<LogtoException>,
    ) {
        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it)
                return@getOidcConfig
            }

            val logtoAuthSession = LogtoAuthSession(
                context = context,
                logtoConfig = logtoConfig,
                oidcConfig = requireNotNull(oidcConfig),
                signInOptions = options,
            ) { authException, fetchedTokenResponse ->
                authException?.let {
                    completion.onComplete(it)
                    return@LogtoAuthSession
                }

                val codeToken = requireNotNull(fetchedTokenResponse)

                val accessToken = AccessToken(
                    codeToken.accessToken,
                    codeToken.scope,
                    expiresAtFrom(nowRoundToSec(), codeToken.expiresIn),
                )

                verifyAndSaveTokenResponse(
                    issuer = oidcConfig.issuer,
                    responseIdToken = codeToken.idToken,
                    responseRefreshToken = codeToken.refreshToken,
                    /**
                     * Treat `scopes` as `null` to construct the default access token key
                     */
                    accessTokenKey = buildAccessTokenKey(),
                    accessToken = accessToken,
                    completion = completion,
                )
            }

            logtoAuthSession.start()
        }
    }

    /**
     * Sign in
     * @param[context] the activity to perform a sign-in action
     * @param[redirectUri] one of the redirect URIs of this application
     * @param[completion] the completion which handles the result of signing in
     */
    fun signIn(
        context: Activity,
        redirectUri: String,
        completion: EmptyCompletion<LogtoException>,
    ) = signIn(
        context = context,
        options = SignInOptions(redirectUri = redirectUri),
        completion = completion,
    )

    /**
     * Sign out
     *
     * Local credentials will be cleared even though there are errors occurred when signing out.
     *
     * @param[completion] the completion which handles the error occurred when signing out
     */
    fun signOut(completion: EmptyCompletion<LogtoException>? = null) {
        if (!isAuthenticated) {
            completion?.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED))
            return
        }

        // Mark - Clear Cookies of WebView
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }

        accessTokenMap.clear()
        idToken = null

        refreshToken?.let { tokenToRevoke ->
            refreshToken = null
            getOidcConfig { getOidcConfigException, oidcConfig ->
                getOidcConfigException?.let {
                    completion?.onComplete(it)
                    return@getOidcConfig
                }
                Core.revoke(
                    revocationEndpoint = requireNotNull(oidcConfig).revocationEndpoint,
                    clientId = logtoConfig.appId,
                    token = tokenToRevoke,
                ) { revokeException ->
                    completion?.onComplete(
                        revokeException?.let {
                            LogtoException(LogtoException.Type.UNABLE_TO_REVOKE_TOKEN, it)
                        },
                    )
                }
            }
        } ?: completion?.onComplete(null)
    }

    /**
     * Get access token without resource and organization id
     * @param[completion] the completion which handles the result
     */
    fun getAccessToken(completion: Completion<LogtoException, AccessToken>) =
        getAccessToken(null, null, completion)

    /**
     * Get access token by resource without a organization id
     * @param[completion] the completion which handles the result
     */
    fun getAccessToken(resource: String?, completion: Completion<LogtoException, AccessToken>) =
        getAccessToken(resource, null, completion)

    /**
     * Get the access token for the specified organization with refresh strategy.
     * Scope `UserScope.Organizations` is required in the config to use organization-related methods.
     */
    fun getOrganizationToken(
        organizationId: String,
        completion: Completion<LogtoException, AccessToken>,
    ) {
        if (!logtoConfig.scopes.contains(UserScope.ORGANIZATIONS)) {
            completion.onComplete(
                LogtoException(LogtoException.Type.MISSING_SCOPE_ORGANIZATIONS),
                null,
            )
            return
        }

        return getAccessToken(null, organizationId, completion)
    }

    /**
     * Get access token
     * @param[resource] the related resource of the retrieving access token
     * @param[completion] the completion which handles the retrieved result
     */
    fun getAccessToken(
        resource: String?,
        organizationId: String?,
        completion: Completion<LogtoException, AccessToken>,
    ) {
        if (!isAuthenticated) {
            completion.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED), null)
            return
        }

        resource?.let {
            if (logtoConfig.resources?.contains(it) == false) {
                completion.onComplete(
                    LogtoException(LogtoException.Type.UNGRANTED_RESOURCE_FOUND).apply { detail = it },
                    null,
                )
                return
            }
        }

        // MARK: Retrieve access token from accessTokenMap
        val accessTokenKey = buildAccessTokenKey(null, resource, organizationId)
        val accessToken = accessTokenMap[accessTokenKey]
        accessToken?.let {
            if (it.expiresAt > nowRoundToSec()) {
                completion.onComplete(null, it)
                return
            }
        }

        // MARK: If cannot refresh the access token, then return a NOT_AUTHENTICATED error
        if (refreshToken == null) {
            completion.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED), null)
            return
        }

        // MARK: If no access token is valid, fetch a new token by refresh token
        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it, null)
                return@getOidcConfig
            }

            Core.fetchTokenByRefreshToken(
                tokenEndpoint = requireNotNull(oidcConfig).tokenEndpoint,
                clientId = logtoConfig.appId,
                refreshToken = requireNotNull(refreshToken),
                resource = resource,
                organizationId = organizationId,
                scopes = null,
            ) { fetchRefreshedTokenException, fetchedTokenResponse ->
                fetchRefreshedTokenException?.let {
                    completion.onComplete(
                        LogtoException(
                            LogtoException.Type.UNABLE_TO_FETCH_TOKEN_BY_REFRESH_TOKEN,
                            it,
                        ),
                        null,
                    )
                    return@fetchTokenByRefreshToken
                }

                val refreshedToken = requireNotNull(fetchedTokenResponse)
                val refreshedAccessToken = AccessToken(
                    token = refreshedToken.accessToken,
                    scope = refreshedToken.scope,
                    expiresAt = expiresAtFrom(
                        nowRoundToSec(),
                        refreshedToken.expiresIn,
                    ),
                )

                verifyAndSaveTokenResponse(
                    issuer = oidcConfig.issuer,
                    responseIdToken = refreshedToken.idToken,
                    responseRefreshToken = refreshedToken.refreshToken,
                    accessTokenKey = buildAccessTokenKey(null, resource, organizationId),
                    accessToken = refreshedAccessToken,
                ) { verifyException ->
                    verifyException?.let { completion.onComplete(it, null) }
                        ?: completion.onComplete(null, refreshedAccessToken)
                }
            }
        }
    }

    /**
     * Get ID token claims
     * @param[completion] the completion which handles the retrieved result
     */
    fun getIdTokenClaims(completion: Completion<LogtoException, IdTokenClaims>) {
        if (!isAuthenticated) {
            completion.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED), null)
            return
        }
        try {
            val idTokenClaims = TokenUtils.decodeIdToken(requireNotNull(idToken))
            completion.onComplete(null, idTokenClaims)
        } catch (exception: InvalidJwtException) {
            completion.onComplete(
                LogtoException(LogtoException.Type.UNABLE_TO_PARSE_ID_TOKEN_CLAIMS, exception),
                null,
            )
        }
    }

    /**
     * Get the organization token claims for the specified organization.
     *
     * @param[organizationId] The ID of the organization that the access token is granted for.
     * @param[completion] the completion which handles the retrieved result
     */
    fun getOrganizationTokenClaims(
        organizationId: String,
        completion: Completion<LogtoException, JwtClaims>,
    ) {
        getOrganizationToken(organizationId) { getOrgTokenException, token ->
            getOrgTokenException?.let {
                completion.onComplete(it, null)
                return@getOrganizationToken
            }

            try {
                val tokenClaims = TokenUtils.decodeToken(requireNotNull(token).token)
                completion.onComplete(null, tokenClaims)
            } catch (exception: InvalidJwtException) {
                completion.onComplete(
                    LogtoException(LogtoException.Type.UNABLE_TO_PARSE_TOKEN_CLAIMS, exception),
                    null,
                )
            }
        }
    }

    /**
     * Fetch user info
     * @param[completion] the completion which handles the retrieved result
     */
    fun fetchUserInfo(completion: Completion<LogtoException, UserInfoResponse>) {
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
                            LogtoException(LogtoException.Type.UNABLE_TO_FETCH_USER_INFO, it),
                            null,
                        )
                        return@fetchUserInfoInCore
                    }
                    completion.onComplete(null, userInfoResponse)
                }
            }
        }
    }

    private fun verifyAndSaveTokenResponse(
        issuer: String,
        responseIdToken: String?,
        responseRefreshToken: String?,
        accessTokenKey: String,
        accessToken: AccessToken,
        completion: EmptyCompletion<LogtoException>,
    ) {
        getJwks { getJwksException, jwks ->
            getJwksException?.let {
                completion.onComplete(it)
                return@getJwks
            }
            responseIdToken?.let {
                try {
                    TokenUtils.verifyIdToken(it, logtoConfig.appId, issuer, requireNotNull(jwks))
                } catch (exception: InvalidJwtException) {
                    completion.onComplete(LogtoException(LogtoException.Type.INVALID_ID_TOKEN, exception))
                    return@getJwks
                }
                idToken = it
            }

            accessTokenMap[accessTokenKey] = accessToken
            refreshToken = responseRefreshToken
            completion.onComplete(null)
        }
    }

    internal fun getOidcConfig(completion: Completion<LogtoException, OidcConfigResponse>) {
        if (oidcConfig != null) {
            completion.onComplete(null, oidcConfig)
            return
        }
        Core.fetchOidcConfig(
            logtoConfig.oidcConfigEndpoint,
        ) { fetchOidcConfigException, oidcConfigResponse ->
            fetchOidcConfigException?.let {
                completion.onComplete(LogtoException(LogtoException.Type.UNABLE_TO_FETCH_OIDC_CONFIG, it), null)
                return@fetchOidcConfig
            }
            oidcConfig = oidcConfigResponse
            completion.onComplete(null, oidcConfig)
        }
    }

    internal fun getJwks(completion: Completion<LogtoException, JsonWebKeySet>) {
        jwks?.let {
            completion.onComplete(null, it)
            return
        }

        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion.onComplete(it, null)
                return@getOidcConfig
            }

            Core.fetchJwksJson(requireNotNull(oidcConfig).jwksUri) { fetchJwksJsonException, jwksJson ->
                fetchJwksJsonException?.let {
                    completion.onComplete(LogtoException(LogtoException.Type.UNABLE_TO_FETCH_JWKS_JSON, it), null)
                    return@fetchJwksJson
                }

                try {
                    jwks = JsonWebKeySet(jwksJson)
                } catch (joseException: JoseException) {
                    completion.onComplete(
                        LogtoException(LogtoException.Type.UNABLE_TO_PARSE_JWKS, joseException),
                        null,
                    )
                    return@fetchJwksJson
                }

                completion.onComplete(null, jwks)
            }
        }
    }

    private fun loadFromStorage() {
        refreshToken = storage?.getItem(StorageKey.REFRESH_TOKEN)
        idToken = storage?.getItem(StorageKey.ID_TOKEN)
    }

    internal fun buildAccessTokenKey(
        scopes: List<String>? = null,
        resource: String? = null,
        organizationId: String? = null,
    ): String {
        val scopesPart = scopes?.sorted()?.joinToString(" ") ?: ""
        val resourcePart = resource ?: ""
        val organizationPart = organizationId?.let { "#$it" } ?: ""
        return "$scopesPart@$resourcePart$organizationPart"
    }

    @TestOnly
    internal fun setupRefreshToken(token: String?) {
        refreshToken = token
    }

    @TestOnly
    internal fun setupIdToken(token: String?) {
        idToken = token
    }

    @TestOnly
    internal fun setupAccessTokenMap(tokenMap: Map<String, AccessToken>) {
        accessTokenMap.putAll(tokenMap)
    }
}
