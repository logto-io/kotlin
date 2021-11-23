package io.logto.client.service

import com.google.gson.FieldNamingPolicy
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.logto.client.constant.GrantType
import io.logto.client.constant.QueryKey
import io.logto.client.exception.LogtoException
import io.logto.client.extensions.httpGet
import io.logto.client.extensions.httpPost
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSetParameters
import org.jose4j.jwk.JsonWebKeySet
import org.json.JSONException

class LogtoService(
    customHttpClient: HttpClient,
) {
    private val httpClient = customHttpClient.config {
        followRedirects = false
        install(JsonFeature) {
            serializer = GsonSerializer() {
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }

    suspend fun fetchOidcConfiguration(domain: String): OidcConfiguration = httpClient.httpGet(
        "https://$domain/oidc/.well-known/openid-configuration",
        LogtoException.REQUEST_OIDC_CONFIGURATION_FAILED,
    )

    suspend fun grantTokenByAuthorizationCode(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
    ): TokenSetParameters = httpClient.httpPost(tokenEndpoint, LogtoException.REQUEST_TOKEN_FAILED) {
        headers {
            contentType(ContentType.Application.FormUrlEncoded)
        }
        body = listOf(
            QueryKey.CLIENT_ID to clientId,
            QueryKey.REDIRECT_URI to redirectUri,
            QueryKey.CODE to code,
            QueryKey.CODE_VERIFIER to codeVerifier,
            QueryKey.GRANT_TYPE to GrantType.AUTHORIZATION_CODE,
        ).formUrlEncode()
    }

    suspend fun grantTokenByRefreshToken(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        refreshToken: String,
    ): TokenSetParameters = httpClient.httpPost(tokenEndpoint, LogtoException.REQUEST_TOKEN_FAILED) {
        headers {
            contentType(ContentType.Application.FormUrlEncoded)
        }
        body = listOf(
            QueryKey.CLIENT_ID to clientId,
            QueryKey.REDIRECT_URI to redirectUri,
            QueryKey.REFRESH_TOKEN to refreshToken,
            QueryKey.GRANT_TYPE to GrantType.REFRESH_TOKEN,
        ).formUrlEncode()
    }

    suspend fun fetchJwks(jwksUri: String): JsonWebKeySet {
        val jsonWebKeySetString: String = httpClient.httpGet(
            jwksUri,
            LogtoException.REQUEST_JWKS_FAILED,
        )
        try {
            return JsonWebKeySet(jsonWebKeySetString)
        } catch (exception: JSONException) {
            throw LogtoException(LogtoException.INVALID_JWKS_JSON, exception)
        }
    }
}
