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
    var expiresAt: Long = 0L

    fun isExpired(): Boolean = TimeUtils.nowRoundToSec() >= expiresAt

    fun expiresInSeconds(): Long = (expiresAt - TimeUtils.nowRoundToSec()).coerceAtLeast(0L)

    fun calculateExpiresAt() {
        expiresAt = TimeUtils.expiresAtFromNow(expiresIn)
    }

    fun validateIdToken(
        clientId: String,
        jwks: JsonWebKeySet,
    ) = TokenUtils.verifyIdToken(idToken, clientId, jwks)

    fun getIdTokenClaims() = TokenUtils.decodeToken(idToken)
}
