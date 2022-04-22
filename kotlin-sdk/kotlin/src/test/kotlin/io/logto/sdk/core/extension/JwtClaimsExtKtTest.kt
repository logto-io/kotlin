package io.logto.sdk.core.extension

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.junit.Test

class JwtClaimsExtKtTest {
    @Test
    fun toIdTokenClaims() {
        val testIssuer = "testIssuer"
        val testSubject = "testSubject"
        val testAudience = listOf("testAudience")
        val testExp = NumericDate.fromSeconds(0L)
        val testIssueAt = NumericDate.fromSeconds(0L)
        val testAtHash = "testAtHash"

        val idTokenClaims = IdTokenClaims(
            iss = testIssuer,
            sub = testSubject,
            aud = testAudience[0],
            exp = testExp.value,
            iat = testIssueAt.value,
            atHash = testAtHash
        )

        val jwtClaims = JwtClaims().apply {
            issuer = testIssuer
            subject = testSubject
            audience = testAudience
            expirationTime = testExp
            issuedAt = testIssueAt
            setClaim(ClaimName.AT_HASH, testAtHash)
        }

        assertThat(jwtClaims.toIdTokenClaims()).isEqualTo(idTokenClaims)
    }
}
