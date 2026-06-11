package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.os.Bundle

/**
 * Receives the redirect delivered by the browser via the `logtoRedirectScheme` intent filter
 * and forwards it to [LogtoBrowserAuthActivity], clearing the Custom Tab off the back stack.
 *
 * The fully qualified name of this activity is public API: integrating apps reference it
 * from their manifests to attach App Links intent filters (or remove the built-in one)
 * through manifest merging. Renaming or moving it is a breaking change.
 */
class LogtoRedirectReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val redirectUri = intent.data
        // This activity is exported: the intent filter matches the scheme and the
        // host, but it only constrains implicit intents — an explicit intent can
        // bypass it and deliver an arbitrary URI. Forwarding is gated on the
        // pending session's redirect URI to keep unrelated intents away from the
        // auth activity; a crafted URI that does match the redirect URI is still
        // forwarded, and rejecting it is up to the session's `state` verification.
        if (redirectUri != null && LogtoAuthManager.isLogtoAuthResult(redirectUri)) {
            startActivity(LogtoBrowserAuthActivity.createRedirectHandlingIntent(this, redirectUri))
        }
        finish()
    }
}
