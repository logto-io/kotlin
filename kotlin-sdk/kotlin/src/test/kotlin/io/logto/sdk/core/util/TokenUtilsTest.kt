package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.util.TokenUtils.DEFAULT_CLOCK_TOLERANCE_IN_SECONDS
import org.jose4j.jwk.EcJwkGenerator
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.ReservedClaimNames
import org.jose4j.jwt.consumer.ErrorCodes
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
    fun `verifyIdToken should complete without exceptions with valid id token`() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()
        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
    }

    @Test
    fun `verifyIdToken should complete without exceptions with valid id token with ES512 format jwks`() {
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
    fun `verifyIdToken should complete without exceptions with issueAt within the clock tolerance`() {
        val jwks = createTestJwks()

        val claimsIssuedInThePast = createTestIdTokenClaims()
        claimsIssuedInThePast.issuedAt = NumericDate.fromSeconds(
            NumericDate.now().value - DEFAULT_CLOCK_TOLERANCE_IN_SECONDS + testTimeDelta,
        )
        val idTokenIssuedInThePast = createTestIdTokenWithClaims(claimsIssuedInThePast)
        TokenUtils.verifyIdToken(idTokenIssuedInThePast, testAudience, testIssuer, jwks)

        val claimsIssuedInTheFuture = createTestIdTokenClaims()
        claimsIssuedInTheFuture.issuedAt = NumericDate.fromSeconds(
            NumericDate.now().value + DEFAULT_CLOCK_TOLERANCE_IN_SECONDS - testTimeDelta,
        )
        val idTokenIssuedInTheFuture = createTestIdTokenWithClaims(claimsIssuedInTheFuture)
        TokenUtils.verifyIdToken(idTokenIssuedInTheFuture, testAudience, testIssuer, jwks)
    }

    @Test
    fun `verifyIdToken should throw with overdue issueAt`() {
        val claims = createTestIdTokenClaims()
        claims.issuedAt = NumericDate.fromSeconds(
            NumericDate.now().value - DEFAULT_CLOCK_TOLERANCE_IN_SECONDS - testTimeDelta,
        )
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException.hasErrorCode(ErrorCodes.ISSUED_AT_INVALID_PAST)).isTrue()
    }

    @Test
    fun `verifyIdToken should throw with issueAt time in the future`() {
        val claims = createTestIdTokenClaims()
        claims.issuedAt = NumericDate.fromSeconds(
            NumericDate.now().value + DEFAULT_CLOCK_TOLERANCE_IN_SECONDS + testTimeDelta,
        )
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException.hasErrorCode(ErrorCodes.ISSUED_AT_INVALID_FUTURE)).isTrue()
    }

    @Test
    fun `verifyIdToken should complete without exceptions with token expired within the clock tolerance`() {
        val expiredSeconds = 100L
        val claims = createTestIdTokenClaims()
        // The expiration time must stay later than the issued-at time, or jose4j rejects the token
        // regardless of the clock tolerance.
        claims.issuedAt = NumericDate.fromSeconds(NumericDate.now().value - 2 * expiredSeconds)
        claims.expirationTime = NumericDate.fromSeconds(NumericDate.now().value - expiredSeconds)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
    }

    @Test
    fun `verifyIdToken should throw with expired token`() {
        val claims = createTestIdTokenClaims()
        claims.expirationTime = NumericDate.fromSeconds(
            NumericDate.now().value - DEFAULT_CLOCK_TOLERANCE_IN_SECONDS - testTimeDelta,
        )
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException.hasExpired()).isTrue()
    }

    @Test
    fun `verifyIdToken should accept a larger custom clock tolerance`() {
        val claims = createTestIdTokenClaims()
        claims.issuedAt = NumericDate.fromSeconds(NumericDate.now().value - 450L)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks, clockTolerance = 600)
    }

    @Test
    fun `verifyIdToken should respect a stricter custom clock tolerance`() {
        val claims = createTestIdTokenClaims()
        claims.issuedAt = NumericDate.fromSeconds(NumericDate.now().value - 100L)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks, clockTolerance = 60)
        }

        assertThat(expectedException.hasErrorCode(ErrorCodes.ISSUED_AT_INVALID_PAST)).isTrue()
    }

    @Test
    fun `verifyIdToken should throw with non-positive clock tolerance`() {
        val idToken = createTestIdTokenWithClaims(createTestIdTokenClaims())
        val jwks = createTestJwks()

        Assert.assertThrows(IllegalArgumentException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks, clockTolerance = 0)
        }
        Assert.assertThrows(IllegalArgumentException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks, clockTolerance = -1)
        }
    }

    @Test
    fun `verifyIdToken should throw with invalid id token`() {
        val idToken = "randomInvalidIdToken"
        val jwks = createTestJwks()

        Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }
    }

    @Test
    fun `verifyIdToken should throw with invalid jwks`() {
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
    fun `verifyIdToken should throw with mismatched issuer`() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer.reversed(), jwks)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(
                "Issuer (iss) claim value ($testIssuer) doesn't match expected value of ${testIssuer.reversed()}",
            )
    }

    @Test
    fun `verifyIdToken should throw with mismatched audience`() {
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
                    "Expected ${testAudience.reversed()} as an aud value.",
            )
    }

    @Test
    fun `verifyIdToken should throw if missing subject`() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.SUBJECT)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Subject (sub) claim is present.")
    }

    @Test
    fun `verifyIdToken should throw if missing expiration time`() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.EXPIRATION_TIME)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Expiration Time (exp) claim present.")
    }

    @Test
    fun `verifyIdToken should throw if missing issuer`() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.ISSUER)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Issuer (iss) claim present.")
    }

    @Test
    fun `verifyIdToken should throw if missing issuedAt`() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.ISSUED_AT)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Issued At (iat) claim present.")
    }

    @Test
    fun `verifyIdToken should throw if missing audience`() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.AUDIENCE)
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()

        val expectedException = Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
        }

        assertThat(expectedException).hasMessageThat().contains("No Audience (aud) claim present.")
    }

    @Test
    fun `decodeIdToken should get expected claims with valid id token`() {
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
    fun `decodeIdToken should throw with invalid token format`() {
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
    fun `decodeIdToken should throw with invalid token payload section`() {
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
