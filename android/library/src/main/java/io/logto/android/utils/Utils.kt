package io.logto.android.utils

import android.net.Uri
import io.logto.android.exception.LogtoException
import io.logto.android.model.TokenSet
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver
import kotlin.math.floor

object Utils {
    fun appendQueryParameters(uriBuilder: Uri.Builder, parameters: Map<String, String>): Uri {
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }
        return uriBuilder.build()
    }

    fun expiresAt(tokenSet: TokenSet): Long {
        return nowRoundToSec() + tokenSet.expiresIn
    }

    fun nowRoundToSec() = floor((System.currentTimeMillis() / 1000L).toDouble()).toLong()

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
                setAllowedClockSkewInSeconds(60)
                setJwsAlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    AlgorithmIdentifiers.RSA_USING_SHA256,
                )
                setVerificationKeyResolver(JwksVerificationKeyResolver(jwks.jsonWebKeys))
            }.build().process(idToken)
        } catch (exception: InvalidJwtException) {
            throw LogtoException(LogtoException.INVALID_JWT, exception)
        }
    }
}
