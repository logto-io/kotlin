package io.logto.android.model

import com.google.gson.annotations.SerializedName

data class Credential(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("id_token") val idToken: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("token_type") val tokenType: String,
)
