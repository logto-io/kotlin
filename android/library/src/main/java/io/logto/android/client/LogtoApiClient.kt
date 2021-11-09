package io.logto.android.client

import io.logto.client.service.LogtoService
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jose4j.jwk.JsonWebKeySet

class LogtoApiClient(
    private val domain: String,
    private val logtoService: LogtoService,
) {
    fun grantTokenByAuthorizationCode(
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
        block: (tokenSet: TokenSet) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfig()
        val jwks = getJsonWebKeySet()
        val tokenSet = logtoService.grantTokenByAuthorizationCode(
            tokenEndpoint = oidcConfiguration.tokenEndpoint,
            clientId = clientId,
            redirectUri = redirectUri,
            code = code,
            codeVerifier = codeVerifier
        ).apply {
            validateIdToken(clientId, jwks)
        }
        block(tokenSet)
    }

    fun grantTokenByRefreshToken(
        clientId: String,
        redirectUri: String,
        refreshToken: String,
        block: (tokenSet: TokenSet) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfig()
        val jwks = getJsonWebKeySet()
        val tokenSet = logtoService.grantTokenByRefreshToken(
            tokenEndpoint = oidcConfiguration.tokenEndpoint,
            clientId = clientId,
            redirectUri = redirectUri,
            refreshToken = refreshToken,
        ).apply {
            validateIdToken(clientId, jwks)
        }
        block(tokenSet)
    }

    fun discover(
        block: (oidcConfiguration: OidcConfiguration) -> Unit
    ) = MainScope().launch {
        block(getOidcConfig())
    }

    private suspend fun getOidcConfig(): OidcConfiguration = coroutineScope {
        oidcConfigCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = logtoService.fetchOidcConfiguration(domain)
        oidcConfigCache = oidcConfiguration
        return@coroutineScope oidcConfiguration
    }

    private suspend fun getJsonWebKeySet(): JsonWebKeySet = coroutineScope {
        jsonWebKeySetCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = getOidcConfig()
        val jsonWebKeySetString = logtoService.fetchJwks(oidcConfiguration.jwksUri)
        val fetchedJsonWebKeySet = JsonWebKeySet(jsonWebKeySetString)
        jsonWebKeySetCache = fetchedJsonWebKeySet
        return@coroutineScope fetchedJsonWebKeySet
    }

    private var oidcConfigCache: OidcConfiguration? = null

    private var jsonWebKeySetCache: JsonWebKeySet? = null
}
