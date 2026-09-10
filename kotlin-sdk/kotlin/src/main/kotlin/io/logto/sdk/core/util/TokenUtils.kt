package io.logto.sdk.core.util

import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver

object TokenUtils {
    /**
     * The default clock tolerance in seconds when verifying the `iat` and `exp` claims of an ID token.
     * It matches the default of the Logto JS SDK.
     */
    const val DEFAULT_CLOCK_TOLERANCE_IN_SECONDS = 300

    /**
     * Verify ID token
     * @param[idToken] The raw string ID token to be verified
     * @param[clientId] The client ID related to this ID token
     * @param[issuer] The ID token issuer
     * @param[jwks] The JSON Web Key Set issued by the Idp
     * @param[clockTolerance] The clock tolerance in seconds for the `iat` and `exp` claims, must be positive
     * @throws[InvalidJwtException]
     * @throws[IllegalArgumentException] if [clockTolerance] is not positive
     */
    @JvmOverloads
    fun verifyIdToken(
        idToken: String,
        clientId: String,
        issuer: String,
        jwks: JsonWebKeySet,
        clockTolerance: Int = DEFAULT_CLOCK_TOLERANCE_IN_SECONDS,
    ) {
        require(clockTolerance > 0) { "clockTolerance must be a positive number of seconds" }
        JwtConsumerBuilder().apply {
            setRequireSubject()
            setRequireExpirationTime()
            setRequireIssuedAt()
            setExpectedIssuer(issuer)
            setExpectedAudience(clientId)
            // jose4j adds the allowed clock skew on top of the `iat` restrictions, so the restrictions
            // are zero to make the accepted `iat` window exactly `now +/- clockTolerance`.
            setAllowedClockSkewInSeconds(clockTolerance)
            setIssuedAtRestrictions(0, 0)
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
