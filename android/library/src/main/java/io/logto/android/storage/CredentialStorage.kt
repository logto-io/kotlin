package io.logto.android.storage

import android.content.Context
import com.google.gson.Gson
import io.logto.android.constant.StorageKey
import io.logto.android.model.Credential

class CredentialStorage(
    context: Context
) {
    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private companion object {
        private const val SHARED_PREFERENCE_NAME = "io.logto.android"
    }

    fun saveCredential(credential: Credential) {
        val credentialJson = Gson().toJson(credential)
        save(StorageKey.CREDENTIAL, credentialJson)
    }

    fun getCredential(): Credential? {
        val credentialJson = get(StorageKey.CREDENTIAL) ?: return null
        return Gson().fromJson(credentialJson, Credential::class.java)
    }

    fun clearCredential() {
        sharedPreferences.edit().remove(StorageKey.CREDENTIAL).apply()
    }

    private fun save(key: String, value: String?) {
        with(sharedPreferences.edit()) {
            if (value == null) {
                remove(key)
            } else {
                putString(key, value)
            }
            apply()
        }
    }

    private fun get(key: String): String? = sharedPreferences.getString(key, null)
}
