package io.logto.android.client

import io.logto.android.LogtoConfig
import io.logto.android.client.api.LogtoClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LogtoClientBuilder(private val logtoConfig: LogtoConfig) {
    fun build(): LogtoClient = Retrofit
        .Builder()
        .baseUrl(logtoConfig.oidcEndpoint)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LogtoClient::class.java)
}
