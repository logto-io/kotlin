package io.logto.android.client

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.logto.android.client.api.LogtoClient
import io.logto.android.config.LogtoConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LogtoClientBuilder(private val logtoConfig: LogtoConfig) {
    fun build(): LogtoClient {
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        return Retrofit
            .Builder()
            .baseUrl(logtoConfig.oidcEndpoint)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LogtoClient::class.java)
    }
}
