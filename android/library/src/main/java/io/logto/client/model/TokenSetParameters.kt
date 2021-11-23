package io.logto.client.model

import io.logto.client.utils.TimeUtils
import io.logto.client.utils.TokenUtils
import org.jose4j.jwk.JsonWebKeySet

data class TokenSetParameters(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
    val expiresIn: Long,
) {
    fun verifyIdToken(clientId: String, jwks: JsonWebKeySet) {
        TokenUtils.verifyIdToken(idToken, clientId, jwks)
    }

    fun convertTokenSet(): TokenSet = TokenSet(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        scope = scope,
        tokenType = tokenType,
        expiresAt = TimeUtils.expiresAtFromNow(expiresIn)
    )
}
