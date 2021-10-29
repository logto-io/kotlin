package io.logto.android.utils

import com.google.common.truth.Truth.assertThat
import io.logto.android.exception.LogtoException
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJsonWebKey
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
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
        val claims = JwtClaims().apply {
            issuer = testIssuer
            setAudience(testAudience)
            subject = testSubject
            setIssuedAtToNow()
            setExpirationTimeMinutesInTheFuture(60F)
            setGeneratedJwtId()
        }

        val idToken = generateIdToken(testRsaJsonWebKey, claims)

        val jwks = createJwks(testRsaJsonWebKey)

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
        val claims = JwtClaims().apply {
            issuer = testIssuer
            setAudience(testAudience)
            setIssuedAtToNow()
            setExpirationTimeMinutesInTheFuture(60F)
            setGeneratedJwtId()
        }
        val idToken = generateIdToken(testRsaJsonWebKey, claims)
        val jwks = createJwks(testRsaJsonWebKey)
        assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
    }

    @Test
    fun verifyIdTokenMissingExpirationTimeShouldThrow() {
        val claims = JwtClaims().apply {
            issuer = testIssuer
            setAudience(testAudience)
            setIssuedAtToNow()
            setExpirationTimeMinutesInTheFuture(60F)
            setGeneratedJwtId()
        }
        val idToken = generateIdToken(testRsaJsonWebKey, claims)
        val jwks = createJwks(testRsaJsonWebKey)
        assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
    }

    @Test
    fun verifyIdTokenMissingIssuedAtShouldThrow() {
        val claims = JwtClaims().apply {
            issuer = testIssuer
            setAudience(testAudience)
            subject = testSubject
            setExpirationTimeMinutesInTheFuture(60F)
            setGeneratedJwtId()
        }
        val idToken = generateIdToken(testRsaJsonWebKey, claims)
        val jwks = createJwks(testRsaJsonWebKey)
        assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
    }

    @Test
    fun verifyIdTokenMissingAudienceShouldThrow() {
        val claims = JwtClaims().apply {
            issuer = testIssuer
            subject = testSubject
            setIssuedAtToNow()
            setExpirationTimeMinutesInTheFuture(60F)
            setGeneratedJwtId()
        }
        val idToken = generateIdToken(testRsaJsonWebKey, claims)
        val jwks = createJwks(testRsaJsonWebKey)
        assertThrows(LogtoException::class.java) {
            Utils.verifyIdToken(idToken, testAudience, jwks)
        }
    }

    private fun generateIdToken(rsaJsonWebKey: RsaJsonWebKey, claims: JwtClaims): String {
        val jws = JsonWebSignature()
        jws.payload = claims.toJson()
        jws.key = rsaJsonWebKey.privateKey
        jws.keyIdHeaderValue = rsaJsonWebKey.keyId
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256;
        return jws.compactSerialization
    }

    private fun createJwks(jsonWebKey: JsonWebKey) = JsonWebKeySet().apply {
        addJsonWebKey(jsonWebKey)
    }
}
