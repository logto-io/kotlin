package io.logto.demo.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.logto.sdk.android.LogtoClient
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.type.IdTokenClaims

class LogtoViewModel(application: Application) : AndroidViewModel(application) {

    private val logtoClient by lazy {
        val logtoConfig = LogtoConfig(
            endpoint = "https://logto.dev",
            clientId = "z4skkM1Z8LLVSl1JCmVZO",
            scopes = null,
            resources = null,
            usingPersistStorage = false
        )
        LogtoClient(logtoConfig)
    }

    private val _authenticated = MutableLiveData(false)
    val authenticated: LiveData<Boolean>
        get() = _authenticated

    private val _accessToken = MutableLiveData<AccessToken>()
    val accessToken: LiveData<AccessToken>
        get() = _accessToken

    private val _idTokenClaims = MutableLiveData<IdTokenClaims>()
    val idTokenClaims: LiveData<IdTokenClaims>
        get() = _idTokenClaims

    private val _exception = MutableLiveData<Throwable>()
    val exception: LiveData<Throwable>
        get() = _exception

    fun signInWithBrowser(context: Activity) {
        logtoClient.signInWithBrowser(context, "io.logto.android://io.logto.sample/callback",) {
            it?.let { _exception.postValue(it) } ?: _authenticated.postValue(logtoClient.isAuthenticated)
        }
    }

    fun signOut() {
        logtoClient.signOut {
            it?.let { _exception.postValue(it) }
            _authenticated.postValue(logtoClient.isAuthenticated)
        }
    }

    fun getAccessToken() {
        logtoClient.getAccessToken { throwable, accessToken ->
            throwable?.let { _exception.postValue(it) } ?: _accessToken.postValue(accessToken)
        }
    }

    fun getIdTokenClaims() {
        logtoClient.getIdTokenClaims { throwable, idTokenClaims ->
            throwable?.let { _exception.postValue(it) } ?: _idTokenClaims.postValue(idTokenClaims)
        }
    }

    fun clearException() {
        _exception.postValue(null)
    }
}
