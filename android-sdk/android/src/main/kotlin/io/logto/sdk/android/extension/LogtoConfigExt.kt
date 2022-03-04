package io.logto.sdk.android.extension

import io.logto.sdk.android.type.LogtoConfig

val LogtoConfig.oidcConfigEndpoint: String
    get() = "$endpoint/oidc/.well-known/openid-configuration"
