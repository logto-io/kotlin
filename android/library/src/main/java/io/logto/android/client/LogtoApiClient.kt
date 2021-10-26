package io.logto.android.client

import io.logto.android.api.LogtoService
import io.logto.android.exception.LogtoException
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jose4j.jwk.JsonWebKeySet

class LogtoApiClient(private val logtoUrl: String) {
    fun grantTokenByAuthorizationCode(
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
        block: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit,
    ) = MainScope().launch {
        try {
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
            block(null, tokenSet)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    fun grantTokenByRefreshToken(
        clientId: String,
        redirectUri: String,
        refreshToken: String,
        block: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit,
    ) = MainScope().launch {
        try {
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
            block(null, tokenSet)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    fun discover(
        block: (exception: LogtoException?, oidcConfiguration: OidcConfiguration?) -> Unit
    ) = MainScope().launch {
        try {
            val oidcConfiguration = getOidcConfig()
            block(null, oidcConfiguration)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    private suspend fun getOidcConfig(): OidcConfiguration = coroutineScope {
        oidcConfigCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = logtoService.discover(logtoUrl)
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

    private val logtoService = LogtoService()

    private var oidcConfigCache: OidcConfiguration? = null

    private var jsonWebKeySetCache: JsonWebKeySet? = null
}
