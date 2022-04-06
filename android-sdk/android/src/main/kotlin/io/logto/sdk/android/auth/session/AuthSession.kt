package io.logto.sdk.android.auth.session

import android.net.Uri
import io.logto.sdk.android.completion.Completion

interface AuthSession<out AuthResult : Any> {
    val completion: Completion<out AuthResult>

    fun start()
    fun handleCallbackUri(callbackUri: Uri)
    fun handleUserCancel()
}
