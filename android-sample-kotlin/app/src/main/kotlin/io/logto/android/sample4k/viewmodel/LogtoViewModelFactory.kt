package io.logto.android.sample4k.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LogtoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogtoViewModel::class.java)) {
            return LogtoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
