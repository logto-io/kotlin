package io.logto.android.storage

import android.content.Context
import com.google.gson.Gson
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.StorageKey
import io.logto.android.model.TokenSet
import io.logto.android.utils.Utils

class TokenSetStorage(
    context: Context,
    logtoConfig: LogtoConfig,
) {
    var tokenSet: TokenSet?
        get() {
            val tokenSetJson = getItem(StorageKey.TOKEN_SET) ?: return null
            return Gson().fromJson(tokenSetJson, TokenSet::class.java)
        }
        set(value) {
            value?.let {
                val tokenSetJson = Gson().toJson(tokenSet)
                setItem(StorageKey.TOKEN_SET, tokenSetJson)
            } ?: setItem(StorageKey.TOKEN_SET, null)
        }

    private val sharedPreferenceName =
        "$SHARED_PREFERENCE_NAME_PREFIX:${Utils.generateHash(logtoConfig.toString())}"

    private val sharedPreferences by lazy {
        context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
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

    private companion object {
        private const val SHARED_PREFERENCE_NAME_PREFIX = "io.logto.android"
    }
}
