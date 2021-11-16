package io.logto.android.callback

import io.logto.client.exception.LogtoException
import io.logto.client.model.OidcConfiguration
import io.logto.client.model.TokenSet
import org.jose4j.jwt.JwtClaims

typealias HandleOidcConfigurationCallback = (exception: LogtoException?, oidcConfiguration: OidcConfiguration?) -> Unit
typealias HandleTokenSetCallback = (exception: LogtoException?, tokenSet: TokenSet?) -> Unit
typealias HandleLogtoExceptionCallback = (exception: LogtoException?) -> Unit
typealias HandleAccessTokenCallback = (exception: LogtoException?, accessToken: String?) -> Unit
typealias HandleIdTokenClaimsCallback = (exception: LogtoException?, idTokenClaims: JwtClaims?) -> Unit
