package io.logto.android.utils

import android.net.Uri
import io.logto.android.exception.LogtoException
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver
import kotlin.math.floor

object Utils {
    private const val MILLIS_PER_SECOND = 1000L
    private const val ALLOWED_CLOCK_SKEW_IN_SECONDS = 60

    fun buildUriWithQueries(baseUrl: String, parameters: Map<String, String>): Uri {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }
        return uriBuilder.build()
    }

    fun expiresAtFrom(startTime: Long, lifetime: Long): Long {
        return startTime + lifetime
    }

    fun expiresAtFromNow(lifetime: Long): Long {
        return expiresAtFrom(nowRoundToSec(), lifetime)
    }

    fun nowRoundToSec() = floor((System.currentTimeMillis() / MILLIS_PER_SECOND).toDouble()).toLong()

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
}
