package io.logto.client.utils

import com.google.common.truth.Truth
import io.logto.client.exception.LogtoException
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.ReservedClaimNames
import org.junit.Assert
import org.junit.Test

class TokenUtilsTest {

    private val testIssuer = "testIssuer"
    private val testAudience = "testAudience"
    private val testSubject = "testSubject"
    private val testRsaJsonWebKey = RsaJwkGenerator.generateJwk(2048).apply {
        keyId = "rsa-json-web-key-id"
    }

    @Test
    fun verifyIdTokenWithValidIdToken() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()

        var logtoException: LogtoException? = null

        try {
            TokenUtils.verifyIdToken(idToken, testAudience, jwks)
        } catch (exception: LogtoException) {
            logtoException = exception
        }

        Truth.assertThat(logtoException).isNull()
    }

    @Test
    fun verifyIdTokenMissingSubjectShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.SUBJECT)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = Assert.assertThrows(LogtoException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, jwks)
        }
        Truth.assertThat(exception).hasMessageThat().contains("No Subject")
    }

    @Test
    fun verifyIdTokenMissingExpirationTimeShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.EXPIRATION_TIME)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = Assert.assertThrows(LogtoException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, jwks)
        }
        Truth.assertThat(exception).hasMessageThat().contains("No Expiration Time")
    }

    @Test
    fun verifyIdTokenMissingIssuedAtShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.ISSUED_AT)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = Assert.assertThrows(LogtoException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, jwks)
        }
        Truth.assertThat(exception).hasMessageThat().contains("No Issued At")
    }

    @Test
    fun verifyIdTokenMissingAudienceShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.AUDIENCE)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = Assert.assertThrows(LogtoException::class.java) {
            TokenUtils.verifyIdToken(idToken, testAudience, jwks)
        }
        Truth.assertThat(exception).hasMessageThat().contains("No Audience")
    }

    @Test
    fun decodeToken() {
        val testIssueAt = NumericDate.now()
        val testExpirationTime = NumericDate.fromSeconds(testIssueAt.value + 60L)
        val expectedTokenClaims = JwtClaims().apply {
            issuer = testIssuer
            setAudience(testAudience)
            subject = testSubject
            issuedAt = testIssueAt
            expirationTime = testExpirationTime
        }
        val testToken = createTestIdToken(expectedTokenClaims)
        val decodedTestToken = TokenUtils.decodeToken(testToken)
        Truth.assertThat(decodedTestToken.issuer).isEqualTo(testIssuer)
        Truth.assertThat(decodedTestToken.audience).contains(testAudience)
        Truth.assertThat(decodedTestToken.subject).isEqualTo(testSubject)
        Truth.assertThat(decodedTestToken.issuedAt).isEqualTo(testIssueAt)
        Truth.assertThat(decodedTestToken.expirationTime).isEqualTo(testExpirationTime)
    }

    @Test
    fun decodeTokenShouldThrowWithInvalidTokenFormat() {
        val invalidToken = "invalidToken"
        val expectedException = Assert.assertThrows(LogtoException::class.java) {
            TokenUtils.decodeToken(invalidToken)
        }
        Truth.assertThat(expectedException).hasMessageThat().isEqualTo(LogtoException.INVALID_JWT)
    }

    @Test
    fun decodeTokenShouldThrowWithInvalideTokenPayloadSection() {
        val invalidToken = "invalidToken.invalidSection"
        val expectedException = Assert.assertThrows(LogtoException::class.java) {
            TokenUtils.decodeToken(invalidToken)
        }
        Truth.assertThat(expectedException).hasMessageThat().contains("Invalid JSON")
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

    private fun createTestIdToken(claims: JwtClaims): String {
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
