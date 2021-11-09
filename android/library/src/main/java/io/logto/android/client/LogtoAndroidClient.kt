package io.logto.android.client

import io.logto.client.LogtoClient
import io.logto.client.config.LogtoConfig
import io.logto.client.service.LogtoService
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jose4j.jwk.JsonWebKeySet

class LogtoAndroidClient(
    logtoConfig: LogtoConfig,
    logtoService: LogtoService,
) : LogtoClient(logtoConfig, logtoService) {
    fun grantTokenByAuthorizationCode(
        authorizationCode: String,
        codeVerifier: String,
        block: (tokenSet: TokenSet) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfiguration()
        val jwks = getJsonWebKeySet()
        val tokenSet = grantTokenByAuthorizationCode(
            oidcConfiguration,
            authorizationCode,
            codeVerifier
        ).apply {
            validateIdToken(logtoConfig.clientId, jwks)
        }
        block(tokenSet)
    }

    fun grantTokenByRefreshToken(
        refreshToken: String,
        block: (tokenSet: TokenSet) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfiguration()
        val jwks = getJsonWebKeySet()
        val tokenSet = grantTokenByRefreshToken(
            oidcConfiguration,
            refreshToken
        ).apply {
            validateIdToken(logtoConfig.clientId, jwks)
        }
        block(tokenSet)
    }

    fun getOidcConfiguration(
        block: (oidcConfiguration: OidcConfiguration) -> Unit
    ) = MainScope().launch {
        block(getOidcConfiguration())
    }

    private suspend fun getOidcConfiguration(): OidcConfiguration = coroutineScope {
        oidcConfigCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = fetchOidcConfiguration()
        oidcConfigCache = oidcConfiguration
        return@coroutineScope oidcConfiguration
    }

    private suspend fun getJsonWebKeySet(): JsonWebKeySet = coroutineScope {
        jsonWebKeySetCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = getOidcConfiguration()
        val jsonWebKeySetString = fetchJwks(oidcConfiguration)
        val fetchedJsonWebKeySet = JsonWebKeySet(jsonWebKeySetString)
        jsonWebKeySetCache = fetchedJsonWebKeySet
        return@coroutineScope fetchedJsonWebKeySet
    }

    private var oidcConfigCache: OidcConfiguration? = null

    private var jsonWebKeySetCache: JsonWebKeySet? = null
}
