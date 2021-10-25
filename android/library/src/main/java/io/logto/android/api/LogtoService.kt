package io.logto.android.api

import com.google.gson.FieldNamingPolicy
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.logto.android.constant.GrantType
import io.logto.android.constant.QueryKey
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet

class LogtoService {

    private val httpClient = HttpClient(Android) {
        followRedirects = false
        install(Logging)
        install(JsonFeature) {
            serializer = GsonSerializer() {
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }

    suspend fun grantTokenByAuthorizationCode(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
    ): TokenSet =
        httpClient.post(tokenEndpoint) {
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
    ): TokenSet =
        httpClient.post(tokenEndpoint) {
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

    suspend fun discover(url: String): OidcConfiguration =
        httpClient.get("$url/oidc/.well-known/openid-configuration")
}
