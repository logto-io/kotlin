package io.logto.sdk.core.util

import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver

object TokenUtils {
    internal const val ISSUED_AT_RESTRICTIONS_IN_SECONDS = 60
    /**
     * Verify ID token
     * @param[idToken] The raw string ID token to be verified
     * @param[clientId] The client ID related to this ID token
     * @param[issuer] The ID token issuer
     * @param[jwks] The JSON Web Key Set issued by the Idp
     * @throws[InvalidJwtException]
     */
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
            setVerificationKeyResolver(JwksVerificationKeyResolver(jwks.jsonWebKeys))
        }.build().process(idToken)
    }

    /**
     * Decode ID token without verification
     * @param[token] the row string ID token to be decoded
     * @return[IdTokenClaims]
     * @throws[InvalidJwtException]
     */
    fun decodeIdToken(token: String): IdTokenClaims = decodeToken(token).toIdTokenClaims()

    /**
     * Decode JWT token without verification
     * @param[token] the row string token to be decoded
     * @return[JwtClaims]
     * @throws[InvalidJwtException]
     */
    fun decodeToken(token: String): JwtClaims = JwtConsumerBuilder().apply {
        setSkipAllValidators()
        setSkipSignatureVerification()
    }.build().processToClaims(token)
}
