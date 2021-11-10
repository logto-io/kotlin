package io.logto.android.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.logging.Logging
import io.logto.client.LogtoClient
import io.logto.client.config.LogtoConfig
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import io.logto.client.service.LogtoService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jose4j.jwk.JsonWebKeySet

class LogtoAndroidClient(
    logtoConfig: LogtoConfig,
) : LogtoClient(
    logtoConfig,
    LogtoService(HttpClient(Android) {
        install(Logging)
    })
) {

    fun getOidcConfigurationAsync(
        block: (oidcConfiguration: OidcConfiguration) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfiguration()
        block(oidcConfiguration)
    }

    fun grantTokenByAuthorizationCodeAsync(
        authorizationCode: String,
        codeVerifier: String,
        block: (tokenSet: TokenSet) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfiguration()
        val jwks = getJsonWebKeySet()
        val tokenSet = grantTokenByAuthorizationCode(
            oidcConfiguration.tokenEndpoint,
            authorizationCode,
            codeVerifier
        ).apply {
            validateIdToken(logtoConfig.clientId, jwks)
        }
        block(tokenSet)
    }

    fun grantTokenByRefreshTokenAsync(
        refreshToken: String,
        block: (tokenSet: TokenSet) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfiguration()
        val jwks = getJsonWebKeySet()
        val tokenSet = grantTokenByRefreshToken(
            oidcConfiguration.tokenEndpoint,
            refreshToken
        ).apply {
            validateIdToken(logtoConfig.clientId, jwks)
        }
        block(tokenSet)
    }

    internal suspend fun getOidcConfiguration(): OidcConfiguration = coroutineScope {
        oidcConfigCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = fetchOidcConfiguration()
        oidcConfigCache = oidcConfiguration
        return@coroutineScope oidcConfiguration
    }

    internal suspend fun getJsonWebKeySet(): JsonWebKeySet = coroutineScope {
        jsonWebKeySetCache?.let {
            return@coroutineScope it
        }
        val oidcConfiguration = getOidcConfiguration()
        val fetchedJsonWebKeySet = fetchJwks(oidcConfiguration)
        jsonWebKeySetCache = fetchedJsonWebKeySet
        return@coroutineScope fetchedJsonWebKeySet
    }

    private var oidcConfigCache: OidcConfiguration? = null

    private var jsonWebKeySetCache: JsonWebKeySet? = null
}
