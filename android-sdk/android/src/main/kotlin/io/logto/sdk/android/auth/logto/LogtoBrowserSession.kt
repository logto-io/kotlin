package io.logto.sdk.android.auth.logto

import android.net.Uri
import io.logto.sdk.android.exception.LogtoException

/**
 * A session which performs a round-trip through the system browser
 * and waits for the browser to redirect back to the app.
 */
interface LogtoBrowserSession {
    /**
     * The URI the browser is expected to redirect back to when this session completes,
     * or `null` if this session does not expect a redirect and can only be finished by
     * the user dismissing the browser.
     */
    val redirectUri: String?

    fun handleCallbackUri(callbackUri: Uri)

    fun handleUserCancel()

    fun handleFailure(exception: LogtoException)
}
