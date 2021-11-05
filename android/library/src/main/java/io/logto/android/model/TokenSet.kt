package io.logto.android.model

import io.logto.android.utils.Utils
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwt.JwtClaims

class TokenSet(tokenSetParameters: TokenSetParameters) {
    val accessToken: String = tokenSetParameters.accessToken
    val refreshToken: String? = tokenSetParameters.refreshToken
    val idToken: String = tokenSetParameters.idToken
    val scope: String = tokenSetParameters.scope
    val tokenType: String = tokenSetParameters.tokenType
    var expiresAt: Long = 0L

    private var expiresIn: Long = tokenSetParameters.expiresIn
        get() = (expiresAt - Utils.nowRoundToSec()).coerceAtLeast(0L)
        private set(seconds) {
            expiresAt = Utils.expiresAtFromNow(seconds)
            field = seconds
        }

    val expired: Boolean = expiresIn == 0L

    fun validateIdToken(
        clientId: String,
        jwks: JsonWebKeySet,
    ) = Utils.verifyIdToken(idToken, clientId, jwks)

    fun getIdTokenClaims(): JwtClaims = Utils.decodeToken(idToken)

    override fun equals(other: Any?): Boolean {
        if (other !is TokenSet) {
            return false
        }
        return (accessToken == other.accessToken &&
                refreshToken.equals(other.refreshToken) &&
                idToken == other.idToken &&
                scope == other.scope &&
                tokenType == other.tokenType &&
                expiresAt == other.expiresAt)
    }

    override fun hashCode(): Int {
        var result = accessToken.hashCode()
        result = 31 * result + (refreshToken?.hashCode() ?: 0)
        result = 31 * result + idToken.hashCode()
        result = 31 * result + scope.hashCode()
        result = 31 * result + tokenType.hashCode()
        result = 31 * result + expiresAt.hashCode()
        return result
    }
}
