package io.logto.demo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.logto.android.Logto
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.ScopeValue
import io.logto.client.exception.LogtoException
import org.jose4j.jwt.JwtClaims

class LogtoViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var logto: Logto

    private val _authenticated = MutableLiveData(false)
    val authenticated: LiveData<Boolean>
        get() = _authenticated

    private val _idTokenClaims = MutableLiveData<JwtClaims>()
    val idTokenClaims: LiveData<JwtClaims>
        get() = _idTokenClaims

    private val _accessToken = MutableLiveData<String>()
    val accessToken: LiveData<String>
        get() = _accessToken

    private val _logtoException = MutableLiveData<LogtoException>()
    val logtoException: LiveData<LogtoException>
        get() = _logtoException

    fun signIn(context: Context) {
        logto.signInWithBrowser(context) { exception, _ ->
            if (exception !== null) {
                _logtoException.postValue(exception)
            } else {
                _authenticated.postValue(logto.authenticated)
            }
        }
    }

    fun signOut(context: Context) {
        logto.signOutWithBrowser(context) { exception ->
            if (exception != null) {
                _logtoException.postValue(exception)
            }
        }
        _authenticated.postValue(logto.authenticated)
    }

    fun getAccessToken() {
        logto.getAccessToken { exception, accessToken ->
            if (exception != null) {
                _logtoException.postValue(exception)
            } else {
                _accessToken.postValue(accessToken)
            }
        }
    }

    fun getIdTokenClaims() {
        logto.getIdTokenClaims { exception, idTokenClaims ->
            if (exception != null) {
                _logtoException.postValue(exception)
            } else {
                _idTokenClaims.postValue(idTokenClaims)
            }
        }
    }

    fun clearException() {
        _logtoException.postValue(null)
    }

    init {
        initLogto()
    }

    private fun initLogto() {
        val logtoConfig = LogtoConfig(
            domain = "logto.dev",
            clientId = "z4skkM1Z8LLVSl1JCmVZO",
            scopeValues = listOf(
                ScopeValue.OPEN_ID,
                ScopeValue.OFFLINE_ACCESS
            ),
            redirectUri = "io.logto.android://io.logto.sample/callback",
            postLogoutRedirectUri = "io.logto.android://io.logto.sample/signout"
        )
        logto = Logto(logtoConfig, getApplication(), true)
        _authenticated.value = logto.authenticated
    }
}
