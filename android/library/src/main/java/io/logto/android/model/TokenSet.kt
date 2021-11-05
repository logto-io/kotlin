package io.logto.android.model

import io.logto.android.utils.Utils
import org.jose4j.jwk.JsonWebKeySet

data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
    private val expiresIn: Long,
) {
    var expiresAt: Long = Utils.expiresAtFromNow(expiresIn)
    fun isExpired(): Boolean = Utils.nowRoundToSec() >= expiresAt
    fun expiresIn(): Long = (expiresAt - Utils.nowRoundToSec()).coerceAtLeast(0L)

    fun validateIdToken(
        clientId: String,
        jwks: JsonWebKeySet,
    ) = Utils.verifyIdToken(idToken, clientId, jwks)

    fun getIdTokenClaims() = Utils.decodeToken(idToken)
}
