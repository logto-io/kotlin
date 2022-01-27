package io.logto.sdk.android.extension

import io.logto.sdk.android.type.LogtoConfig

val LogtoConfig.oidcConfigEndpoint: String
    get() = "$endpoint/.well-known/openid-configuration"
