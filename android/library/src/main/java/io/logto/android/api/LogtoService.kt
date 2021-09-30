package io.logto.android.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.logto.android.constant.QueryKey
import io.logto.android.model.Credential
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LogtoService {
    @FormUrlEncoded
    @POST("token")
    suspend fun getCredential(
        @Field(QueryKey.REDIRECT_URI) redirectUri: String,
        @Field(QueryKey.CODE) code: String,
        @Field(QueryKey.GRANT_TYPE) grantType: String,
        @Field(QueryKey.CLIENT_ID) clientId: String,
        @Field(QueryKey.CODE_VERIFIER) codeVerifier: String,
    ): Credential

    companion object {
        fun create(endpoint: String): LogtoService {
            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            return Retrofit
                .Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(LogtoService::class.java)
        }
    }
}
