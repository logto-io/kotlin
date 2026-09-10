package io.logto.sdk.android.type

import io.logto.sdk.core.util.TokenUtils

/**
 * The options for verifying the ID token received from the Logto server.
 */
class IdTokenVerificationOptions @JvmOverloads constructor(
    /**
     * The clock tolerance in seconds when verifying the `iat` and `exp` claims of the ID token.
     *
     * Sign-in and token refresh fail with `LogtoException.Type.INVALID_ID_TOKEN` on devices whose clock
     * drifts from the Logto server by more than this value. Defaults to
     * [TokenUtils.DEFAULT_CLOCK_TOLERANCE_IN_SECONDS] (5 minutes), the same default as the Logto JS SDK.
     */
    val clockTolerance: Int = TokenUtils.DEFAULT_CLOCK_TOLERANCE_IN_SECONDS,
) {
    init {
        require(clockTolerance > 0) { "clockTolerance must be a positive number of seconds" }
    }
}
