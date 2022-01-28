package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.util.TokenUtils.ISSUED_AT_RESTRICTIONS_IN_SECONDS
import org.jose4j.jwk.EcJwkGenerator
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.ReservedClaimNames
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.keys.EllipticCurves
import org.junit.Assert
import org.junit.Test

class TokenUtilsTest {
    private val testIssuer = "testIssuer"
    private val testAudience = "testAudience"
    private val testSubject = "testSubject"
    private val testAtHash = "testAtHash"
    private val testRsaJsonWebKey = RsaJwkGenerator.generateJwk(2048).apply {
        keyId = "rsa-json-web-key-id"
    }
    private val testTimeDelta = 10L

    @Test
    fun verifyIdTokenWithValidIdToken() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()
        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
    }

    @Test
    fun verifyIdTokenWithValidIdTokenWithES512JsonWebKey() {
        val claims = createTestIdTokenClaims()

        // Note: Es512 is "ECDSA using P-521 and SHA-512".
        // Rfc: https://datatracker.ietf.org/doc/html/rfc7518#section-3.1
        val es512JsonWebKey = EcJwkGenerator.generateJwk(EllipticCurves.P521).apply {
            keyId = "es512-json-web-key-id"
        }
        val jws = JsonWebSignature().apply {
            payload = claims.toJson()
            key = es512JsonWebKey.privateKey
            keyIdHeaderValue = es512JsonWebKey.keyId
            algorithmHeaderValue = AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512
        }
        val idToken = jws.compactSerialization

        val jwks = JsonWebKeySet().apply {
            addJsonWebKey(es512JsonWebKey)
        }

        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
    }

    @Test
    fun verifyIdTokenWithOverdueIssueAtShouldThrow() {
        val claims = createTestIdTokenClaims()
        claims.issuedAt = NumericDate.fromSeconds(
            NumericDate.now().value - ISSUED_AT_RESTRICTIONS_IN_SECONDS.toLong() - testTimeDelta
        )
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains("more than $ISSUED_AT_RESTRICTIONS_IN_SECONDS second(s) in the past.")
    }

    @Test
    fun verifyIdTokenWithFutureIssueAtShouldThrow() {
        val claims = createTestIdTokenClaims()
        claims.issuedAt = NumericDate.fromSeconds(
            NumericDate.now().value + ISSUED_AT_RESTRICTIONS_IN_SECONDS.toLong() + testTimeDelta
        )
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains("more than $ISSUED_AT_RESTRICTIONS_IN_SECONDS second(s) ahead of now")
    }

    @Test
    fun verifyIdTokenWithExpiredTokenShouldThrow() {
        val claims = createTestIdTokenClaims()
        claims.expirationTime = NumericDate.fromSeconds(
            NumericDate.now().value - testTimeDelta
        )
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("on or after the Expiration Time")
    }

    @Test
    fun verifyIdTokenWithInvalidIdTokenShouldThrow() {
        val idToken = "randomInvalidIdToken"
        val jwks = createTestJwks()

        Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }
    }

    @Test
    fun verifyIdTokenWithInvalidJwksShouldThrow() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val anotherRsaJwk = RsaJwkGenerator.generateJwk(2048).apply {
            keyId = "another-key-id"
        }
        val anotherJwks = JsonWebKeySet().apply {
            addJsonWebKey(anotherRsaJwk)
        }

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, anotherJwks)
        }

        assertThat(expectedException).hasMessageThat().contains("Unable to find a suitable verification key for JWS")
    }

    @Test
    fun verifyIdTokenMismatchedIssuerShouldThrow() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer.reversed(), jwks)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(
                "Issuer (iss) claim value ($testIssuer) doesn't match expected value of ${testIssuer.reversed()}"
            )
    }

    @Test
    fun verifyIdTokenMismatchedAudienceShouldThrow() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience.reversed(), testIssuer, jwks)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(
                "Audience (aud) claim [$testAudience] doesn't contain an acceptable identifier. " +
                    "Expected ${testAudience.reversed()} as an aud value."
            )
    }

    @Test
    fun verifyIdTokenMissingSubjectShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.SUBJECT)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Subject (sub) claim is present.")
    }

    @Test
    fun verifyIdTokenMissingExpirationTimeShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.EXPIRATION_TIME)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Expiration Time (exp) claim present.")
    }

    @Test
    fun verifyIdTokenMissingIssuerShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.ISSUER)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Issuer (iss) claim present.")
    }

    @Test
    fun verifyIdTokenMissingIssuedAtShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.ISSUED_AT)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Issued At (iat) claim present.")
    }

    @Test
    fun verifyIdTokenMissingAudienceShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.AUDIENCE)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Audience (aud) claim present.")
    }

    @Test
    fun decodeIdToken() {
        val testIssueAt = NumericDate.now()
        val testExpirationTime = NumericDate.fromSeconds(testIssueAt.value + 60L)
        val testClaims = JwtClaims().apply {
            issuer = testIssuer
            setAudience(testAudience)
            subject = testSubject
            issuedAt = testIssueAt
            expirationTime = testExpirationTime
            setStringClaim(ClaimName.AT_HASH, testAtHash)
        }
        val testToken = createTestIdTokenWithClaims(testClaims)

        val decodedTestToken = TokenUtils.decodeIdToken(testToken)

        assertThat(decodedTestToken).isEqualTo(testClaims.toIdTokenClaims())
    }

    @Test
    fun decodeIdTokenShouldThrowWithInvalidTokenFormat() {
        val invalidTokenWithOnePart = "invalidToken"
        val expectedExceptionOnePart = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.decodeIdToken(invalidTokenWithOnePart)
        }
        assertThat(expectedExceptionOnePart).hasMessageThat().contains("Invalid JOSE Compact Serialization.")

        val invalidTokenWithTwoParts = "invalidToken.invalidToken"
        val expectedExceptionTwoParts = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.decodeIdToken(invalidTokenWithTwoParts)
        }
        assertThat(expectedExceptionTwoParts).hasMessageThat().contains("Invalid JOSE Compact Serialization.")
    }

    @Test
    fun decodeIdTokenShouldThrowWithInvalidTokenPayloadSection() {
        val tokenWithInvalidPayload = "part1.invalidPayload.part3"
        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.decodeIdToken(tokenWithInvalidPayload)
        }
        assertThat(expectedException).hasMessageThat().contains("Parsing error")
    }

    private fun createTestIdTokenClaims() = JwtClaims().apply {
        issuer = testIssuer
        setAudience(testAudience)
        subject = testSubject
        setIssuedAtToNow()
        setExpirationTimeMinutesInTheFuture(60F)
        setGeneratedJwtId()
    }

    private fun createTestIdTokenClaimsWithoutDistinctClaim(unsetClaimName: String) =
        createTestIdTokenClaims().apply {
            unsetClaim(unsetClaimName)
        }

    private fun createTestIdTokenWithClaims(claims: JwtClaims): String {
        val jws = JsonWebSignature().apply {
            payload = claims.toJson()
            key = testRsaJsonWebKey.privateKey
            keyIdHeaderValue = testRsaJsonWebKey.keyId
            algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        }
        return jws.compactSerialization
    }

    private fun createTestJwks() = JsonWebKeySet().apply {
        addJsonWebKey(testRsaJsonWebKey)
    }
}
