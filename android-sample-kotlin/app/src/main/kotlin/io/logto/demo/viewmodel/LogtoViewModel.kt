package io.logto.demo.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.logto.sdk.android.LogtoClient
import io.logto.sdk.android.exception.LogtoException
import io.logto.sdk.android.type.AccessToken
import io.logto.sdk.android.type.LogtoConfig
import io.logto.sdk.core.type.IdTokenClaims

class LogtoViewModel(application: Application) : AndroidViewModel(application) {

    private val logtoConfig = LogtoConfig(
        endpoint = "https://logto.dev",
        clientId = "94fKrpteyMI6BAy9K3pdX",
        scopes = null,
        resources = null,
        usingPersistStorage = true
    )

    private val logtoClient = LogtoClient(logtoConfig, getApplication())

    private val _authenticated = MutableLiveData(logtoClient.isAuthenticated)
    val authenticated: LiveData<Boolean>
        get() = _authenticated

    private val _accessToken = MutableLiveData<AccessToken>()
    val accessToken: LiveData<AccessToken>
        get() = _accessToken

    private val _idTokenClaims = MutableLiveData<IdTokenClaims>()
    val idTokenClaims: LiveData<IdTokenClaims>
        get() = _idTokenClaims

    private val _logtoException = MutableLiveData<LogtoException>()
    val logtoException: LiveData<LogtoException>
        get() = _logtoException

    fun signInWithBrowser(context: Activity) {
        logtoClient.signInWithBrowser(context, "io.logto.android://io.logto.sample/callback",) {
            it?.let { _logtoException.postValue(it) } ?: _authenticated.postValue(logtoClient.isAuthenticated)
        }
    }

    fun signOut() {
        logtoClient.signOut {
            it?.let { _logtoException.postValue(it) }
            _authenticated.postValue(logtoClient.isAuthenticated)
        }
    }

    fun getAccessToken() {
        logtoClient.getAccessToken { logtoException, accessToken ->
            logtoException?.let { _logtoException.postValue(it) } ?: _accessToken.postValue(accessToken)
        }
    }

    fun getIdTokenClaims() {
        logtoClient.getIdTokenClaims { logtoException, idTokenClaims ->
            logtoException?.let { _logtoException.postValue(it) } ?: _idTokenClaims.postValue(idTokenClaims)
        }
    }

    fun clearException() {
        _logtoException.postValue(null)
    }
}
