package io.logto.client.model

import io.logto.client.utils.TimeUtils
import io.logto.client.utils.TokenUtils
import org.jose4j.jwk.JsonWebKeySet

data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
    private val expiresIn: Long,
) {
    var expiresAt: Long = TimeUtils.expiresAtFromNow(expiresIn)
    fun isExpired(): Boolean = TimeUtils.nowRoundToSec() >= expiresAt
    fun expiresIn(): Long = (expiresAt - TimeUtils.nowRoundToSec()).coerceAtLeast(0L)

    fun validateIdToken(
        clientId: String,
        jwks: JsonWebKeySet,
    ) = TokenUtils.verifyIdToken(idToken, clientId, jwks)

    fun getIdTokenClaims() = TokenUtils.decodeToken(idToken)
}
