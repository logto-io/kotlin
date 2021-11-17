package io.logto.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private val logtoViewModel: LogtoViewModel by lazy {
        ViewModelProvider(this, LogtoViewModelFactory(application))
            .get(LogtoViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        logtoViewModel.authenticated.observe(this, Observer { hasAuthenticated ->
            if (hasAuthenticated) {
                navigateToTokenSetActivity()
            }
        })
    }

    private fun initViews() {
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            logtoViewModel.signIn(this)
        }
    }

    private fun navigateToTokenSetActivity() {
        startActivity(Intent(this, TokenSetActivity::class.java))
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
