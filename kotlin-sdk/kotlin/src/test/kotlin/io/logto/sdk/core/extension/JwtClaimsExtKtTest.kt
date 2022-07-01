package io.logto.sdk.core.extension

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.junit.Test

class JwtClaimsExtKtTest {
    @Test
    fun `toIdTokenClaims should return excepted id token claims`() {
        val testIssuer = "testIssuer"
        val testSubject = "testSubject"
        val testAudience = listOf("testAudience")
        val testExp = NumericDate.fromSeconds(0L)
        val testIssueAt = NumericDate.fromSeconds(0L)
        val testAtHash = "testAtHash"
        val testName = "testName"
        val testUsername = "testUsername"
        val testAvatar = "testAvatar"
        val testRoleNames = listOf("testRoleNames")

        val idTokenClaims = IdTokenClaims(
            iss = testIssuer,
            sub = testSubject,
            aud = testAudience[0],
            exp = testExp.value,
            iat = testIssueAt.value,
            atHash = testAtHash,
            name = testName,
            username = testUsername,
            avatar = testAvatar,
            roleNames = testRoleNames,
        )

        val jwtClaims = JwtClaims().apply {
            issuer = testIssuer
            subject = testSubject
            audience = testAudience
            expirationTime = testExp
            issuedAt = testIssueAt
            setClaim(ClaimName.AT_HASH, testAtHash)
            setClaim(ClaimName.NAME, testName)
            setClaim(ClaimName.USERNAME, testUsername)
            setClaim(ClaimName.AVATAR, testAvatar)
            setStringListClaim(ClaimName.ROLE_NAMES, testRoleNames)
        }

        assertThat(jwtClaims.toIdTokenClaims()).isEqualTo(idTokenClaims)
    }
}
