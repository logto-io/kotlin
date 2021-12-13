package io.logto.android.storage

import android.content.Context
import com.google.gson.Gson
import io.logto.client.constant.StorageKey
import io.logto.client.model.TokenSet

class TokenSetStorage(
    context: Context,
    sharedPreferencesName: String,
) {
    var tokenSet: TokenSet?
        get() {
            val tokenSetJson = getItem(StorageKey.TOKEN_SET) ?: return null
            return Gson().fromJson(tokenSetJson, TokenSet::class.java)
        }
        set(value) {
            value?.let {
                val tokenSetJson = Gson().toJson(value)
                setItem(StorageKey.TOKEN_SET, tokenSetJson)
            } ?: setItem(StorageKey.TOKEN_SET, null)
        }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    }

    private fun getItem(key: String): String? = sharedPreferences.getString(key, null)

    private fun setItem(key: String, value: String?) {
        with(sharedPreferences.edit()) {
            if (value == null) {
                remove(key)
            } else {
                putString(key, value)
            }
            apply()
        }
    }
}
