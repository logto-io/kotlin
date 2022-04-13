package io.logto.sdk.core.extension

import io.logto.sdk.core.constant.ClaimName
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.jwt.JwtClaims

fun JwtClaims.toIdTokenClaims(): IdTokenClaims = IdTokenClaims(
    iss = this.issuer,
    sub = this.subject,
    aud = this.audience[0],
    exp = this.expirationTime.value,
    iat = this.issuedAt.value,
    atHash = this.getClaimValueAsString(ClaimName.AT_HASH),
)
