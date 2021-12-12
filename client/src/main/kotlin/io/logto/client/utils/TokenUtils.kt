package io.logto.client.utils

import io.logto.client.exception.LogtoException
import org.jose4j.base64url.Base64Url
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver

object TokenUtils {
    private const val ALLOWED_CLOCK_SKEW_IN_SECONDS = 60

    fun verifyIdToken(
        idToken: String,
        audience: String,
        jwks: JsonWebKeySet,
    ) {
        try {
            JwtConsumerBuilder().apply {
                setRequireSubject()
                setRequireExpirationTime()
                setRequireIssuedAt()
                setExpectedAudience(audience)
                setAllowedClockSkewInSeconds(ALLOWED_CLOCK_SKEW_IN_SECONDS)
                setJwsAlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    AlgorithmIdentifiers.RSA_USING_SHA256,
                )
                setVerificationKeyResolver(JwksVerificationKeyResolver(jwks.jsonWebKeys))
            }.build().process(idToken)
        } catch (exception: InvalidJwtException) {
            throw LogtoException(
                "${LogtoException.INVALID_JWT}: ${exception.message}",
                exception
            )
        }
    }

    fun decodeToken(token: String): JwtClaims {
        val sections = token.split('.')
        if (sections.size < 2) {
            throw LogtoException(LogtoException.INVALID_JWT)
        }
        try {
            val payloadSection = sections[1]
            val payloadJson = Base64Url.decodeToUtf8String(payloadSection)
            return JwtClaims.parse(payloadJson)
        } catch (exception: InvalidJwtException) {
            throw LogtoException(
                "${LogtoException.INVALID_JWT}: ${exception.message}",
                exception
            )
        }
    }
}
