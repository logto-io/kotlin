package io.logto.android.utils

import com.google.common.truth.Truth.assertThat
import io.logto.android.exception.LogtoException
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.ReservedClaimNames
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilsTest {

    private val testIssuer = "testIssuer"
    private val testAudience = "testAudience"
    private val testSubject = "testSubject"
    private val testRsaJsonWebKey = RsaJwkGenerator.generateJwk(2048).apply {
        keyId = "rsa-json-web-key-id"
    }

    @Test
    fun buildUriWithQueries() {
        val baseUrl = "logto.io"
        val queries = mapOf(
            "key1" to "value1",
            "key2" to "value2",
        )
        val uri = Utils.buildUriWithQueries(baseUrl, queries)
        assertThat(uri.getQueryParameter("key1")).isEqualTo("value1")
        assertThat(uri.getQueryParameter("key2")).isEqualTo("value2")
    }

    @Test
    fun expiresAtFrom() {
        val startTime = 1000L
        val lifeTime = 1000L
        val expectedExpiresAt = 2000L
        assertThat(Utils.expiresAtFrom(startTime, lifeTime)).isEqualTo(expectedExpiresAt)
    }

    @Test
    fun verifyIdTokenWithValidIdToken() {
        val claims = createTestIdTokenClaims()
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()

        var logtoException: LogtoException? = null

        try {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        } catch (exception: LogtoException) {
            logtoException = exception
        }

        assertThat(logtoException).isNull()
    }

    @Test
    fun verifyIdTokenMissingSubjectShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.SUBJECT)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
        assertThat(exception).hasMessageThat().contains("No Subject")
    }

    @Test
    fun verifyIdTokenMissingExpirationTimeShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.EXPIRATION_TIME)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
        assertThat(exception).hasMessageThat().contains("No Expiration Time")
    }

    @Test
    fun verifyIdTokenMissingIssuedAtShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.ISSUED_AT)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
        assertThat(exception).hasMessageThat().contains("No Issued At")
    }

    @Test
    fun verifyIdTokenMissingAudienceShouldThrow() {
        val claims = createTestIdTokenClaimsWithoutDistinctClaim(ReservedClaimNames.AUDIENCE)
        val idToken = createTestIdToken(claims)
        val jwks = createTestJwks()
        val exception = assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
        assertThat(exception).hasMessageThat().contains("No Audience")
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
