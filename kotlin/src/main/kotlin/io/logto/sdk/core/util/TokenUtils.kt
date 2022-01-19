package io.logto.sdk.core.util

import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder

object TokenUtils {
    fun decodeIdToken(token: String): IdTokenClaims = JwtConsumerBuilder().apply {
        setSkipAllValidators()
        setSkipSignatureVerification()
    }.build().process(token).jwtClaims.toIdTokenClaims()
}
