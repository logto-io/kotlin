package io.logto.android.client

import io.logto.android.api.LogtoService
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LogtoApiClient(private val logtoUrl: String) {
    fun grantTokenByAuthorizationCode(
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
        block: (exception: Exception?, tokenSet: TokenSet?) -> Unit,
    ) = MainScope().launch {
        try {
            val oidcConfiguration = getOidcConfig()
            val tokenSet = logtoService.grantTokenByAuthorizationCode(
                tokenEndpoint = oidcConfiguration.tokenEndpoint,
                clientId = clientId,
                redirectUri = redirectUri,
                code = code,
                codeVerifier = codeVerifier
            )
            block(null, tokenSet)
        } catch (exception: Exception) {
            block(exception, null)
        }
    }

    fun grantTokenByRefreshToken(
        clientId: String,
        redirectUri: String,
        refreshToken: String,
        block: (exception: Exception?, tokenSet: TokenSet?) -> Unit,
    ) = MainScope().launch {
        try {
            val oidcConfiguration = getOidcConfig()
            val tokenSet = logtoService.grantTokenByRefreshToken(
                tokenEndpoint = oidcConfiguration.tokenEndpoint,
                clientId = clientId,
                redirectUri = redirectUri,
                refreshToken = refreshToken,
            )
            block(null, tokenSet)
        } catch (exception: Exception) {
            block(exception, null)
        }
    }

    fun discover(
        block: (oidcConfig: OidcConfiguration) -> Unit
    ) = MainScope().launch {
        val oidcConfig = getOidcConfig()
        block(oidcConfig)
    }

    private suspend fun getOidcConfig(): OidcConfiguration = coroutineScope {
        oidcConfigCache?.let {
            return@coroutineScope it
        }
        try {
            val oidcConfiguration = logtoService.discover(logtoUrl)
            oidcConfigCache = oidcConfiguration
            return@coroutineScope oidcConfiguration
        } catch (exception: Exception) {
            throw exception
        }
    }

    private val logtoService = LogtoService()

    private var oidcConfigCache: OidcConfiguration? = null
}
