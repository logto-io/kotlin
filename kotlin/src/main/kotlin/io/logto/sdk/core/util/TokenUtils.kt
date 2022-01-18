package io.logto.sdk.core.util

import io.logto.sdk.core.exception.LogtoException
import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.base64url.Base64Url
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver

object TokenUtils {
    fun decodeIdToken(token: String): IdTokenClaims {
        val sections = token.split('.')
        if (sections.size < 2) {
            throw LogtoException(LogtoException.INVALID_JWT)
        }
        try {
            val payloadSection = sections[1]
            val payloadJson = Base64Url.decodeToUtf8String(payloadSection)
            return JwtClaims.parse(payloadJson).toIdTokenClaims()
        } catch (exception: InvalidJwtException) {
            throw LogtoException(
                "${LogtoException.INVALID_JWT}: ${exception.message}",
                exception
            )
        }
    }
}
