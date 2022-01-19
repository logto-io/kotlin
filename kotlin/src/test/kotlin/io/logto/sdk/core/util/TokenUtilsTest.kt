package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.extension.toIdTokenClaims
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
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

    private fun createTestIdTokenWithClaims(claims: JwtClaims): String {
        val jws = JsonWebSignature()
        jws.payload = claims.toJson()
        jws.key = testRsaJsonWebKey.privateKey
        jws.keyIdHeaderValue = testRsaJsonWebKey.keyId
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        return jws.compactSerialization
    }
}
