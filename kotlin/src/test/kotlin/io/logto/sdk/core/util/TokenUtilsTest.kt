package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.extension.toIdTokenClaims
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.ReservedClaimNames
import org.jose4j.jwt.consumer.InvalidJwtException
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

    @Test
    fun verifyIdTokenWithValidIdToken() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdTokenWithClaims(claims)
        val jwks = createTestJwks()
        TokenUtils.verifyIdToken(idToken, testAudience, testIssuer, jwks)
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
        val jws = JsonWebSignature()
        jws.payload = claims.toJson()
        jws.key = testRsaJsonWebKey.privateKey
        jws.keyIdHeaderValue = testRsaJsonWebKey.keyId
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        return jws.compactSerialization
    }

    private fun createTestJwks() = JsonWebKeySet().apply {
        addJsonWebKey(testRsaJsonWebKey)
    }
}
