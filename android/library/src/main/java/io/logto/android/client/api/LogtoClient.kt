package io.logto.android.client.api

import io.logto.android.constant.AuthConstant
import io.logto.android.model.Credential
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LogtoClient {
    @FormUrlEncoded
    @POST("token")
    fun getCredential(
        @Field(AuthConstant.QueryKey.REDIRECT_URI) redirectUri: String,
        @Field(AuthConstant.QueryKey.CODE) code: String,
        @Field(AuthConstant.QueryKey.GRANT_TYPE) grantType: String,
        @Field(AuthConstant.QueryKey.CLIENT_ID) clientId: String,
        @Field(AuthConstant.QueryKey.CODE_VERIFIER) codeVerifier: String,
    ): Call<Credential>
}
