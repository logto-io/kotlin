package io.logto.android.sample4k

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.logto.android.sample4k.viewmodel.LogtoViewModel
import io.logto.android.sample4k.viewmodel.LogtoViewModelFactory

class MainActivity : AppCompatActivity() {

    private val logtoViewModel: LogtoViewModel by viewModels {
        LogtoViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        logtoViewModel.logtoException.observe(this) { logtoException ->
            logtoException?.let {
                Toast.makeText(this, "${logtoException.message}", Toast.LENGTH_LONG).show()
                logtoViewModel.clearException()
            }
        }
    }
}
