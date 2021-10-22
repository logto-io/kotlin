package io.logto.android.client

import com.google.gson.FieldNamingPolicy
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Parameters
import io.logto.android.constant.GrantType
import io.logto.android.constant.QueryKey
import io.logto.android.model.OidcConfiguration
import io.logto.android.model.TokenSet

class LogtoClient {
    private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer() {
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }

    // TODO: remove this comment once ktor version upgrades to 2.0.0
    // `append` usage warning: https://youtrack.jetbrains.com/issue/KTOR-3001#focus=Comments-27-5105259.0-0
    suspend fun grantTokenByAuthorizationCode(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        code: String,
        codeVerifier: String,
    ): TokenSet =
        httpClient.post(tokenEndpoint) {
            body = FormDataContent(
                Parameters.build {
                    append(QueryKey.CLIENT_ID, clientId)
                    append(QueryKey.REDIRECT_URI, redirectUri)
                    append(QueryKey.CODE, code)
                    append(QueryKey.CODE_VERIFIER, codeVerifier)
                    append(QueryKey.GRANT_TYPE, GrantType.AUTHORIZATION_CODE)
                }
            )
        }

    // TODO: remove this comment once ktor version upgrades to 2.0.0
    // `append` usage warning: https://youtrack.jetbrains.com/issue/KTOR-3001#focus=Comments-27-5105259.0-0
    suspend fun grantTokenByRefreshToken(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        refreshToken: String,
    ): TokenSet =
        httpClient.post(tokenEndpoint) {
            body = FormDataContent(
                Parameters.build {
                    append(QueryKey.CLIENT_ID, clientId)
                    append(QueryKey.REDIRECT_URI, redirectUri)
                    append(QueryKey.REFRESH_TOKEN, refreshToken)
                    append(QueryKey.GRANT_TYPE, GrantType.REFRESH_TOKEN)
                }
            )
        }

    suspend fun discover(url: String): OidcConfiguration =
        httpClient.get("$url/oidc/.well-known/openid-configuration")
}
