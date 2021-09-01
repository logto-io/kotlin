package io.logto.android.client.api

import io.logto.android.model.Credential
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LogtoClient {
    @FormUrlEncoded
    @POST("token")
    fun getCredential(
        @Field("redirect_uri") redirectUri: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("code_verifier") codeVerifier: String,
    ): Call<Credential>
}
