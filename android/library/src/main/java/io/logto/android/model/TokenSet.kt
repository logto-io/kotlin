package io.logto.android.model

import io.logto.android.utils.Utils
import org.jose4j.jwk.JsonWebKeySet

data class TokenSet(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String?,
    val idToken: String,
    val scope: String,
    val tokenType: String,
) {
    fun validateIdToken(
        clientId: String,
        jwks: JsonWebKeySet,
    ) = Utils.verifyIdToken(idToken, clientId, jwks)
}
