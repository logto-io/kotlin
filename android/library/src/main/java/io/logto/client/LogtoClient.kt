package io.logto.client

import io.ktor.http.URLBuilder
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.CodeChallengeMethod
import io.logto.client.constant.PromptValue
import io.logto.client.constant.QueryKey
import io.logto.client.constant.ResourceValue
import io.logto.client.constant.ResponseType
import io.logto.client.model.OidcConfiguration

class LogtoClient(
    private val logtoConfig: LogtoConfig,
) {
    fun getSignInUrl(
        oidcConfiguration: OidcConfiguration,
        codeChallenge: String,
    ): String {
        val endpoint = oidcConfiguration.authorizationEndpoint
        val urlBuilder = URLBuilder(endpoint).apply {
            parameters.append(QueryKey.CLIENT_ID, logtoConfig.clientId)
            parameters.append(QueryKey.CODE_CHALLENGE, codeChallenge)
            parameters.append(QueryKey.CODE_CHALLENGE_METHOD, CodeChallengeMethod.S256)
            parameters.append(QueryKey.PROMPT, PromptValue.CONSENT)
            parameters.append(QueryKey.REDIRECT_URI, logtoConfig.redirectUri)
            parameters.append(QueryKey.RESPONSE_TYPE, ResponseType.CODE)
            parameters.append(QueryKey.SCOPE, logtoConfig.encodedScopes)
            parameters.append(QueryKey.RESOURCE, ResourceValue.LOGTO_API)
        }
        return urlBuilder.buildString()
    }
}
