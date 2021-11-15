package io.logto.android.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.logging.Logging
import io.logto.client.LogtoClient
import io.logto.client.config.LogtoConfig
import io.logto.client.exception.LogtoException
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import io.logto.client.service.LogtoService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jose4j.jwk.JsonWebKeySet

class LogtoAndroidClient(
    logtoConfig: LogtoConfig,
) : LogtoClient(
    logtoConfig,
    LogtoService(HttpClient(Android) {
        install(Logging)
    })
) {
    private val coroutineScope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.IO +
                CoroutineName("logto-android")
    )

    fun getOidcConfigurationAsync(
        block: (exception: LogtoException?, oidcConfiguration: OidcConfiguration?) -> Unit,
    ) = coroutineScope.launch {
        try {
            val oidcConfiguration = getOidcConfiguration()
            block(null, oidcConfiguration)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    fun grantTokenByAuthorizationCodeAsync(
        authorizationCode: String,
        codeVerifier: String,
        block: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit,
    ) = coroutineScope.launch {
        try {
            val oidcConfiguration = getOidcConfiguration()
            val jwks = getJsonWebKeySet()
            val tokenSet = grantTokenByAuthorizationCode(
                oidcConfiguration.tokenEndpoint,
                authorizationCode,
                codeVerifier
            ).apply {
                validateIdToken(logtoConfig.clientId, jwks)
            }
            block(null, tokenSet)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    fun grantTokenByRefreshTokenAsync(
        refreshToken: String,
        block: (exception: LogtoException?, tokenSet: TokenSet?) -> Unit,
    ) = coroutineScope.launch {
        try {
            val oidcConfiguration = getOidcConfiguration()
            val jwks = getJsonWebKeySet()
            val tokenSet = grantTokenByRefreshToken(
                oidcConfiguration.tokenEndpoint,
                refreshToken
            ).apply {
                validateIdToken(logtoConfig.clientId, jwks)
            }
            block(null, tokenSet)
        } catch (exception: LogtoException) {
            block(exception, null)
        }
    }

    internal suspend fun getOidcConfiguration(): OidcConfiguration = withContext(
        coroutineScope.coroutineContext
    ) {
        oidcConfigCache?.let {
            return@withContext it
        }
        val oidcConfiguration = fetchOidcConfiguration()
        oidcConfigCache = oidcConfiguration
        return@withContext oidcConfiguration
    }

    internal suspend fun getJsonWebKeySet(): JsonWebKeySet = withContext(
        coroutineScope.coroutineContext
    ) {
        jsonWebKeySetCache?.let {
            return@withContext it
        }
        val oidcConfiguration = getOidcConfiguration()
        val fetchedJsonWebKeySet = fetchJwks(oidcConfiguration)
        jsonWebKeySetCache = fetchedJsonWebKeySet
        return@withContext fetchedJsonWebKeySet
    }

    private var oidcConfigCache: OidcConfiguration? = null

    private var jsonWebKeySetCache: JsonWebKeySet? = null
}
