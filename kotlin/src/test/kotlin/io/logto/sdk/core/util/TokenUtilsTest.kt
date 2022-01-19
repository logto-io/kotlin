package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.exception.LogtoException
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

        assertThat(decodedTestToken.iss).isEqualTo(testIssuer)
        assertThat(decodedTestToken.aud).contains(testAudience)
        assertThat(decodedTestToken.sub).isEqualTo(testSubject)
        assertThat(decodedTestToken.iat).isEqualTo(testIssueAt.value)
        assertThat(decodedTestToken.exp).isEqualTo(testExpirationTime.value)
        assertThat(decodedTestToken.atHash).isEqualTo(testAtHash)
    }

    @Test
    fun decodeIdTokenShouldThrowWithInvalidTokenFormat() {
        val invalidToken = "invalidToken"
        val expectedException = Assert.assertThrows(LogtoException.DecodingException::class.java) {
            TokenUtils.decodeIdToken(invalidToken)
        }
        assertThat(expectedException).hasMessageThat().isEqualTo(LogtoException.Decoding.INVALID_JWT.name)
    }

    @Test
    fun decodeIdTokenShouldThrowWithInvalidTokenPayloadSection() {
        val tokenWithInvalidPayload = "invalidToken.invalidPayload"
        Assert.assertThrows(InvalidJwtException::class.java) {
            TokenUtils.decodeIdToken(tokenWithInvalidPayload)
        }
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
