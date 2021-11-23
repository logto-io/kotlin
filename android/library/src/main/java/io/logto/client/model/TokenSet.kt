package io.logto.client.model

import io.logto.client.utils.TimeUtils
import io.logto.client.utils.TokenUtils

data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
    val expiresAt: Long,
) {
    fun isExpired(): Boolean = TimeUtils.nowRoundToSec() >= expiresAt

    fun getIdTokenClaims() = TokenUtils.decodeToken(idToken)
}
