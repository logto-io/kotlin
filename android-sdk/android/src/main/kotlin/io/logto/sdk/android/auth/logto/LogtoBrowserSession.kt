package io.logto.sdk.android.auth.logto

import android.net.Uri
import io.logto.sdk.android.exception.LogtoException

/**
 * A session which performs a round-trip through the system browser and completes when
 * the browser redirects back to the app or the user dismisses the browser; each session
 * decides how a dismissal is reported.
 */
interface LogtoBrowserSession {
    /**
     * The URI the browser is expected to redirect back to when this session completes,
     * or `null` if this session expects no redirect — the user typically finishes it
     * by dismissing the browser.
     */
    val redirectUri: String?

    fun handleCallbackUri(callbackUri: Uri)

    fun handleUserCancel()

    fun handleFailure(exception: LogtoException)
}
