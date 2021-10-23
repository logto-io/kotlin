package io.logto.android.client

import io.logto.android.api.LogtoService
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.Error

class LogtoApiClient(private val logtoUrl: String) {
    fun grantTokenByAuthorizationCode(
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
        block: (error: Error?, tokenSet: TokenSet?) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfig()
        try {
            val tokenSet = logtoService.grantTokenByAuthorizationCode(
                tokenEndpoint = oidcConfiguration.tokenEndpoint,
                clientId = clientId,
                redirectUri = redirectUri,
                code = code,
                codeVerifier = codeVerifier
            )
            block(null, tokenSet)
        } catch (exception: Exception) {
            block(Error(exception.message), null)
        }
    }

    fun grantTokenByRefreshToken(
        clientId: String,
        redirectUri: String,
        refreshToken: String,
        block: (error: Error?, tokenSet: TokenSet?) -> Unit,
    ) = MainScope().launch {
        val oidcConfiguration = getOidcConfig()
        try {
            val tokenSet = logtoService.grantTokenByRefreshToken(
                tokenEndpoint = oidcConfiguration.tokenEndpoint,
                clientId = clientId,
                redirectUri = redirectUri,
                refreshToken = refreshToken,
            )
            block(null, tokenSet)
        } catch (exception: Exception) {
            block(Error(exception.message), null)
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
            throw Error(exception.message)
        }
    }

    private val logtoService = LogtoService()

    private var oidcConfigCache: OidcConfiguration? = null
}
