package io.logto.android.auth

import android.content.Context
import android.net.Uri

interface IFlow {
    fun start(context: Context)
    fun handleRedirectUri(redirectUri: Uri)
}
