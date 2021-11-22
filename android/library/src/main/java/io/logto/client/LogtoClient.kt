package io.logto.client

import io.ktor.http.URLBuilder
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.CodeChallengeMethod
import io.logto.client.constant.PromptValue
import io.logto.client.constant.QueryKey
import io.logto.client.constant.ResourceValue
import io.logto.client.constant.ResponseType
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import io.logto.client.service.LogtoService

open class LogtoClient(
    val logtoConfig: LogtoConfig,
    private val logtoService: LogtoService,
) {
    fun getSignInUrl(
        authorizationEndpoint: String,
        codeChallenge: String,
    ): String {
        val urlBuilder = URLBuilder(authorizationEndpoint).apply {
            parameters.append(QueryKey.CLIENT_ID, logtoConfig.clientId)
            parameters.append(QueryKey.CODE_CHALLENGE, codeChallenge)
            parameters.append(QueryKey.CODE_CHALLENGE_METHOD, CodeChallengeMethod.S256)
            parameters.append(QueryKey.PROMPT, PromptValue.CONSENT)
            parameters.append(QueryKey.REDIRECT_URI, logtoConfig.redirectUri)
            parameters.append(QueryKey.RESPONSE_TYPE, ResponseType.CODE)
            parameters.append(QueryKey.RESOURCE, ResourceValue.LOGTO_API)
            parameters.append(QueryKey.SCOPE, logtoConfig.scope)
        }
        return urlBuilder.buildString()
    }

    fun getSignOutUrl(
        endSessionEndpoint: String,
        idToken: String,
    ): String {
        val urlBuilder = URLBuilder(endSessionEndpoint).apply {
            parameters.append(QueryKey.ID_TOKEN_HINT, idToken)
            parameters.append(QueryKey.POST_LOGOUT_REDIRECT_URI, logtoConfig.postLogoutRedirectUri)
        }
        return urlBuilder.buildString()
    }

    suspend fun fetchOidcConfiguration(): OidcConfiguration =
        logtoService.fetchOidcConfiguration(logtoConfig.domain)

    suspend fun grantTokenByAuthorizationCode(
        tokenEndpoint: String,
        authorizationCode: String,
        codeVerifier: String,
    ): TokenSet = logtoService.grantTokenByAuthorizationCode(
        tokenEndpoint = tokenEndpoint,
        clientId = logtoConfig.clientId,
        redirectUri = logtoConfig.redirectUri,
        code = authorizationCode,
        codeVerifier = codeVerifier
    )

    suspend fun grantTokenByRefreshToken(
        tokenEndpoint: String,
        refreshToken: String,
    ): TokenSet = logtoService.grantTokenByRefreshToken(
        tokenEndpoint = tokenEndpoint,
        clientId = logtoConfig.clientId,
        redirectUri = logtoConfig.redirectUri,
        refreshToken = refreshToken
    )

    suspend fun fetchJwks(oidcConfiguration: OidcConfiguration) =
        logtoService.fetchJwks(oidcConfiguration.jwksUri)
}
