package io.logto.sdk.android.storage

import android.content.Context

class PersistStorage(
    context: Context,
    storageName: String,
) {
    private val sharedPreferences by lazy {
        context.getSharedPreferences(storageName, Context.MODE_PRIVATE)
    }

    fun getItem(key: String) = sharedPreferences.getString(key, null)

    fun setItem(key: String, value: String?) = sharedPreferences.edit().apply {
        value?.let { putString(key, value) } ?: remove(key)
        apply()
    }
}
