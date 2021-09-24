package io.logto.android.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.logto.android.constant.AuthConstant
import io.logto.android.model.Credential
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LogtoService {
    @FormUrlEncoded
    @POST("token")
    suspend fun getCredential(
        @Field(AuthConstant.QueryKey.REDIRECT_URI) redirectUri: String,
        @Field(AuthConstant.QueryKey.CODE) code: String,
        @Field(AuthConstant.QueryKey.GRANT_TYPE) grantType: String,
        @Field(AuthConstant.QueryKey.CLIENT_ID) clientId: String,
        @Field(AuthConstant.QueryKey.CODE_VERIFIER) codeVerifier: String,
    ): Response<Credential>

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
