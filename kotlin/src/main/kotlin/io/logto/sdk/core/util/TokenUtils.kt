package io.logto.sdk.core.util

import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver

object TokenUtils {
    internal const val ISSUED_AT_RESTRICTIONS_IN_SECONDS = 60

    fun verifyIdToken(
        idToken: String,
        clientId: String,
        issuer: String,
        jwks: JsonWebKeySet,
    ) {
        JwtConsumerBuilder().apply {
            setRequireSubject()
            setRequireExpirationTime()
            setRequireIssuedAt()
            setExpectedIssuer(issuer)
            setExpectedAudience(clientId)
            setIssuedAtRestrictions(ISSUED_AT_RESTRICTIONS_IN_SECONDS, ISSUED_AT_RESTRICTIONS_IN_SECONDS)
            setJwsAlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT,
                AlgorithmIdentifiers.RSA_USING_SHA256,
            )
            setVerificationKeyResolver(JwksVerificationKeyResolver(jwks.jsonWebKeys))
        }.build().process(idToken)
    }

    fun decodeIdToken(token: String): IdTokenClaims = JwtConsumerBuilder().apply {
        setSkipAllValidators()
        setSkipSignatureVerification()
    }.build().process(token).jwtClaims.toIdTokenClaims()
}
