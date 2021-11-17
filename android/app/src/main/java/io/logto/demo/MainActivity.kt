package io.logto.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.Logto
import io.logto.client.config.LogtoConfig
import io.logto.client.constant.ScopeValue

class MainActivity : AppCompatActivity() {

    private lateinit var logto: Logto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButtons()
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

    private fun initButtons() {
        findViewById<Button>(R.id.signin_button).setOnClickListener {
            signIn()
        }

        findViewById<Button>(R.id.signout_button).setOnClickListener {
            signOut()
        }

        findViewById<Button>(R.id.get_access_token_button).setOnClickListener {
            getAccessToken()
        }

        findViewById<Button>(R.id.refresh_token_button).setOnClickListener {
            refreshAccessToken()
        }
    }

    private fun signIn() {
        logto.signInWithBrowser(this) { exception, tokenSet ->
            if (exception !== null) {
                Log.d(TAG, "Sign in Failed: $exception")
                return@signInWithBrowser
            } else {
                Log.d(TAG, "Sign in Success!")
                Log.d(TAG, "TokenSet After Sign in: $tokenSet")
            }
        }
    }

    private fun signOut() {
        logto.signOutWithBrowser(this) { exception ->
            if (exception != null) {
                Log.d(TAG, "Sign out with exception: $exception")
                return@signOutWithBrowser
            } else {
                Log.d(TAG, "Sign out Success!")
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
