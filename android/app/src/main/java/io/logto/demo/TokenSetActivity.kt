package io.logto.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import org.json.JSONObject

class TokenSetActivity : AppCompatActivity() {

    private lateinit var idTokenClaimsTextView: TextView

    private val logtoViewModel: LogtoViewModel by lazy {
        ViewModelProvider(this, LogtoViewModelFactory(application))
            .get(LogtoViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_set)
        supportActionBar?.hide()
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        logtoViewModel.logtoException.observe(this) { exception ->
            if (exception != null) {
                Toast.makeText(this, "${exception.message}", Toast.LENGTH_LONG).show()
                logtoViewModel.clearException()
            }
        }

        logtoViewModel.idTokenClaims.observe(this) { idTokenCliams ->
            if (idTokenCliams != null) {
                idTokenClaimsTextView.text = JSONObject(idTokenCliams.toJson()).toString(2)
            } else {
                idTokenClaimsTextView.text = null
            }
        }

        logtoViewModel.accessToken.observe(this) { accessToken ->
            Toast.makeText(this, "AccessToken: $accessToken", Toast.LENGTH_LONG).show()
        }

        logtoViewModel.authenticated.observe(this) { authenticated ->
            if (authenticated) {
                logtoViewModel.getIdTokenClaims()
            } else {
                finish()
            }
        }
    }

    private fun initViews() {
        idTokenClaimsTextView = findViewById(R.id.token_claims_text)

        findViewById<Button>(R.id.get_access_token_button).setOnClickListener {
            logtoViewModel.getAccessToken()
        }

        findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            logtoViewModel.signOut(this)
        }
    }
}
