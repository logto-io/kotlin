package io.logto.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.Logto
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.ScopeValue

class MainActivity : AppCompatActivity() {

    private lateinit var logto: Logto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val logtoConfig = LogtoConfig(
            domain = "logto.dev",
            clientId = "z4skkM1Z8LLVSl1JCmVZO",
            scopes = listOf(
                ScopeValue.OPEN_ID,
                ScopeValue.OFFLINE_ACCESS
            ),
            redirectUri = "io.logto.android://io.logto.sample/callback",
            postLogoutRedirectUri = "io.logto.android://io.logto.sample/signout"
        )
        logto = Logto(logtoConfig, application, true)
    }

    fun onLoginButtonClick(view: View) {
        login()
    }

    fun onLogoutButtonClick(view: View) {
        logout()
    }

    fun onGetAccessTokenClick(view: View) {
        getAccessToken()
    }

    fun onRefreshTokenClick(view: View) {
        refreshAccessToken()
    }

    private fun login() {
        logto.signInWithBrowser(this) { exception, tokenSet ->
            if (exception !== null) {
                Log.d(TAG, "Login Failed: $exception")
                return@signInWithBrowser
            } else {
                Log.d(TAG, "Login Success!")
                Log.d(TAG, "TokenSet After Login: $tokenSet")
            }
        }
    }

    private fun logout() {
        logto.signOutWithBrowser(this) { exception ->
            if (exception != null) {
                Log.d(TAG, "Logout with exception: $exception")
                return@signOutWithBrowser
            } else {
                Log.d(TAG, "Logout Success!")
            }
        }
    }

    private fun getAccessToken() {
        logto.getAccessToken { exception, accessToken ->
            if (exception !== null) {
                Log.d(TAG, "Get Accesss Token Failed: $exception")
            } else {
                Log.d(TAG, "Get Accesss Token Success: $accessToken")
            }
        }
    }

    private fun refreshAccessToken() {
        logto.refreshTokenSet { exception, tokenSet ->
            if (exception != null) {
                Log.d(TAG, "Refresh Credential Failed: $exception")
            } else {
                Log.d(TAG, "Refresh Credential Success: $tokenSet")
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
