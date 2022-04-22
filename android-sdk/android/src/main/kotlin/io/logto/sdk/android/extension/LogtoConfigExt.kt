package io.logto.sdk.android.extension

import android.net.Uri
import io.logto.sdk.android.type.LogtoConfig

val LogtoConfig.oidcConfigEndpoint: String
    get() = Uri.parse(endpoint)
        .buildUpon()
        .appendEncodedPath("oidc/.well-known/openid-configuration")
        .toString()
