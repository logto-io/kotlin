package io.logto.android.callback

import io.logto.client.exception.LogtoException
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet

typealias OidcConfigurationCallback = (exception: LogtoException?, oidcConfiguration: OidcConfiguration?) -> Unit
typealias TokenSetCallback = (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
