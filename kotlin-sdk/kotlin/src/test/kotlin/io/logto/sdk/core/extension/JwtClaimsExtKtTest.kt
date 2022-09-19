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
        val testPicture = "testPicture"
        val testRoleNames = listOf("testRoleNames")
        val testEmail = "testEmail"
        val testEmailVerified = true
        val testPhone = "testPhone"
        val testPhoneVerified = true

        val idTokenClaims = IdTokenClaims(
            iss = testIssuer,
            sub = testSubject,
            aud = testAudience[0],
            exp = testExp.value,
            iat = testIssueAt.value,
            atHash = testAtHash,
            name = testName,
            username = testUsername,
            picture = testPicture,
            roleNames = testRoleNames,
            email = testEmail,
            emailVerified = testEmailVerified,
            phoneNumber = testPhone,
            phoneNumberVerified = testPhoneVerified,
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
            setClaim(ClaimName.PICTURE, testPicture)
            setStringListClaim(ClaimName.ROLE_NAMES, testRoleNames)
            setClaim(ClaimName.EMAIL, testEmail)
            setClaim(ClaimName.EMAIL_VERIFIED, testEmailVerified)
            setClaim(ClaimName.PHONE_NUMBER, testPhone)
            setClaim(ClaimName.PHONE_NUMBER_VERIFIED, testPhoneVerified)
        }

        assertThat(jwtClaims.toIdTokenClaims()).isEqualTo(idTokenClaims)
    }
}
