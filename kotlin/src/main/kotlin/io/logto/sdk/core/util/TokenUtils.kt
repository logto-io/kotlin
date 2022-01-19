package io.logto.sdk.core.util

import io.logto.sdk.core.exception.LogtoException
import io.logto.sdk.core.extension.toIdTokenClaims
import io.logto.sdk.core.type.IdTokenClaims
import org.jose4j.base64url.Base64Url
import org.jose4j.jwt.JwtClaims

object TokenUtils {
    fun decodeIdToken(token: String): IdTokenClaims {
        val sections = token.split('.')
        if (sections.size < 2) {
            throw LogtoException.DecodingException(LogtoException.Decoding.INVALID_JWT)
        }
        val payloadSection = sections[1]
        val payloadJson = Base64Url.decodeToUtf8String(payloadSection)
        return JwtClaims.parse(payloadJson).toIdTokenClaims()
    }
}
