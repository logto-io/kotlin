package io.logto.sdk.android.auth.social

import android.app.Activity
import io.logto.sdk.android.completion.Completion

interface SocialSession {
    val context: Activity
    val redirectTo: String
    val callbackUri: String
    val completion: Completion<SocialException, String>

    fun start()
}
