package io.logto.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.logto.demo.viewmodel.LogtoViewModel
import io.logto.demo.viewmodel.LogtoViewModelFactory

class MainActivity : AppCompatActivity() {

    private val logtoViewModel: LogtoViewModel by viewModels {
        LogtoViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        logtoViewModel.exception.observe(this) { exception ->
            exception?.let {
                Toast.makeText(this, "${exception.message}", Toast.LENGTH_LONG).show()
                logtoViewModel.clearException()
            }
        }
    }
}
