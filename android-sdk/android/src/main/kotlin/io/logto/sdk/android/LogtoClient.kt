package io.logto.sdk.android

import android.app.Activity
import android.app.Application
import io.logto.sdk.android.auth.logto.LogtoAuthSession
import io.logto.sdk.android.auth.logto.LogtoSignOutSession
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
import io.logto.sdk.android.util.LogtoUtils.isValidRedirectUri
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
     * Guards the credential fields below: token flows that were in flight when
     * [signOut] or [clearCredentials] dropped the credentials must not persist
     * their (now stale) results. See [SessionGuard].
     */
    private val sessionGuard = SessionGuard()

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
     *
     * If a sign-out happens while the sign-in is still in progress, the sign-in result
     * is discarded and the completion receives a
     * [LogtoException.Type.NOT_AUTHENTICATED] error.
     *
     * @param[context] the activity to perform a sign-in action
     * @param[options] the sign-in options
     * @param[completion] the completion which handles the result of signing in
     */
    fun signIn(
        context: Activity,
        options: SignInOptions,
        completion: EmptyCompletion<LogtoException>,
    ) {
        val sessionStamp = sessionGuard.stamp()

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
                    sessionStamp = sessionStamp,
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
     * Clear local credentials: drop the cached access tokens and the ID token, and attempt
     * to revoke the refresh token.
     *
     * Local credentials are cleared even if the revocation fails or cannot be attempted.
     *
     * Note: this does NOT end the session on the Logto server — the session cookie lives in
     * the browser, which is not accessible to the app, so the next [signIn] may silently
     * re-enter the account through the existing browser session. Use [signOut] for a
     * complete sign-out.
     *
     * Any token request that is still in flight when the credentials are cleared is
     * discarded: its result is not persisted and its completion receives a
     * [LogtoException.Type.NOT_AUTHENTICATED] error.
     *
     * @param[completion] the completion invoked with any error that occurs while clearing
     * the credentials
     */
    fun clearCredentials(completion: EmptyCompletion<LogtoException>? = null) {
        if (!isAuthenticated) {
            completion?.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED))
            return
        }

        dropCredentials()?.let { tokenToRevoke ->
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
     * Sign out: clear local credentials, revoke the refresh token if one is present, and
     * end the session on the Logto server by opening the end session endpoint in the browser.
     *
     * Local credentials are cleared as soon as the sign-out starts, so the local session
     * always ends even if fetching the OIDC config fails or the browser flow is abandoned.
     *
     * When a refresh token is present its revocation settles before the end session
     * endpoint is opened in the browser. When [postLogoutRedirectUri] is provided, the
     * browser navigates back to the app through it after the session ends — register the
     * URI in the Logto console first; it must match an intent filter declared for the
     * app, either the SDK's built-in `logtoRedirectScheme` custom-scheme filter or an
     * App Links filter the app declares itself. When omitted, the browser shows the
     * Logto sign-out page and the user dismisses it manually.
     *
     * Dismissing the browser is never reported as [LogtoException.Type.USER_CANCELED]:
     * the local sign-out has already taken effect by the time the browser opens.
     * Failures of the earlier steps, such as a failed revocation, are still reported
     * through the [completion].
     *
     * An invalid [postLogoutRedirectUri] is reported as
     * [LogtoException.Type.INVALID_REDIRECT_URI] without opening the browser; local
     * credentials are still cleared and the revocation is still attempted.
     *
     * Any token request that is still in flight when the sign-out starts is discarded:
     * its result is not persisted and its completion receives a
     * [LogtoException.Type.NOT_AUTHENTICATED] error.
     *
     * @param[context] the activity to perform the sign-out action
     * @param[postLogoutRedirectUri] one of the post sign-out redirect URIs of this
     * application, or `null` to let the user dismiss the browser manually after the
     * session ends
     * @param[completion] the completion invoked with any error that occurs while signing out
     */
    fun signOut(
        context: Activity,
        postLogoutRedirectUri: String? = null,
        completion: EmptyCompletion<LogtoException>? = null,
    ) {
        if (!isAuthenticated) {
            completion?.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED))
            return
        }

        if (postLogoutRedirectUri != null && !isValidRedirectUri(postLogoutRedirectUri)) {
            clearCredentials { completion?.onComplete(LogtoException(LogtoException.Type.INVALID_REDIRECT_URI)) }
            return
        }

        val tokenToRevoke = dropCredentials()

        getOidcConfig { getOidcConfigException, oidcConfig ->
            getOidcConfigException?.let {
                completion?.onComplete(it)
                return@getOidcConfig
            }

            val fetchedOidcConfig = requireNotNull(oidcConfig)

            fun startBrowserSignOut(revokeException: LogtoException?) {
                val signOutSession = LogtoSignOutSession(
                    context = context,
                    signOutUri = Core.generateSignOutUri(
                        endSessionEndpoint = fetchedOidcConfig.endSessionEndpoint,
                        clientId = logtoConfig.appId,
                        postLogoutRedirectUri = postLogoutRedirectUri,
                    ),
                    postLogoutRedirectUri = postLogoutRedirectUri,
                ) { browserException ->
                    completion?.onComplete(browserException ?: revokeException)
                }
                signOutSession.start()
            }

            tokenToRevoke?.let { token ->
                Core.revoke(
                    revocationEndpoint = fetchedOidcConfig.revocationEndpoint,
                    clientId = logtoConfig.appId,
                    token = token,
                ) { revocationException ->
                    startBrowserSignOut(
                        revocationException?.let {
                            LogtoException(LogtoException.Type.UNABLE_TO_REVOKE_TOKEN, it)
                        },
                    )
                }
            } ?: startBrowserSignOut(null)
        }
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
        // The stamp must be taken before any credential is read: a sign-out that lands
        // between the read and the stamp would otherwise go unnoticed and the refreshed
        // tokens would be committed against the already-cleared credentials.
        val sessionStamp = sessionGuard.stamp()

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
        // Snapshot the refresh token: a concurrent sign-out can null the field while this
        // flow is between its async hops; the flow runs on the snapshot and the session
        // guard arbitrates at commit time.
        val tokenForRefresh = refreshToken
        if (tokenForRefresh == null) {
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
                refreshToken = tokenForRefresh,
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
                    sessionStamp = sessionStamp,
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
        // Snapshot the ID token: a concurrent sign-out can null the field at any point
        val currentIdToken = idToken
        if (!isAuthenticated || currentIdToken == null) {
            completion.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED), null)
            return
        }
        try {
            val idTokenClaims = TokenUtils.decodeIdToken(currentIdToken)
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

    /**
     * Atomically drop the local credentials and invalidate the token flows that are
     * still in flight, so that their responses can no longer be persisted.
     *
     * @return the refresh token that was current, for the caller to revoke
     */
    private fun dropCredentials(): String? = sessionGuard.invalidate {
        val tokenToRevoke = refreshToken
        accessTokenMap.clear()
        idToken = null
        refreshToken = null
        tokenToRevoke
    }

    private fun verifyAndSaveTokenResponse(
        sessionStamp: Int,
        issuer: String,
        responseIdToken: String?,
        responseRefreshToken: String?,
        accessTokenKey: String,
        accessToken: AccessToken,
        completion: EmptyCompletion<LogtoException>,
    ) {
        // Discard already-stale flows before fetching the JWKS or verifying the response
        if (!sessionGuard.isCurrent(sessionStamp)) {
            completion.onComplete(LogtoException(LogtoException.Type.NOT_AUTHENTICATED))
            return
        }

        getJwks { getJwksException, jwks ->
            val verificationException = getJwksException ?: verifyIdToken(responseIdToken, issuer, jwks)

            val saved = verificationException == null &&
                sessionGuard.commit(sessionStamp) {
                    responseIdToken?.let { idToken = it }
                    accessTokenMap[accessTokenKey] = accessToken
                    refreshToken = responseRefreshToken
                }

            completion.onComplete(
                when {
                    saved -> null
                    // Stale flows always complete with NOT_AUTHENTICATED, even when the
                    // response would also have failed verification
                    !sessionGuard.isCurrent(sessionStamp) ->
                        LogtoException(LogtoException.Type.NOT_AUTHENTICATED)
                    else -> verificationException
                },
            )
        }
    }

    private fun verifyIdToken(
        responseIdToken: String?,
        issuer: String,
        jwks: JsonWebKeySet?,
    ): LogtoException? = responseIdToken?.let {
        try {
            TokenUtils.verifyIdToken(it, logtoConfig.appId, issuer, requireNotNull(jwks))
            null
        } catch (exception: InvalidJwtException) {
            LogtoException(LogtoException.Type.INVALID_ID_TOKEN, exception)
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

/**
 * An optimistic guard for the local credential set — the in-memory equivalent of an
 * optimistic lock's "UPDATE ... WHERE version = ?".
 *
 * Async token flows take a [stamp] when they start, and [commit] applies their writes
 * only when no [invalidate] has happened in between. This keeps a token response that
 * lands after a sign-out from resurrecting the cleared credentials, and a response
 * from before a sign-out from clobbering the session of a later sign-in.
 */
private class SessionGuard {
    private var version = 0

    @Synchronized
    fun stamp(): Int = version

    @Synchronized
    fun isCurrent(stamp: Int): Boolean = stamp == version

    @Synchronized
    fun <T> invalidate(block: () -> T): T {
        version++
        return block()
    }

    @Synchronized
    fun commit(stamp: Int, block: () -> Unit): Boolean {
        if (stamp != version) {
            return false
        }
        block()
        return true
    }
}
