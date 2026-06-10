package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.os.Bundle

/**
 * Receives the redirect delivered by the browser via the `logtoRedirectScheme` intent filter
 * and forwards it to [LogtoBrowserAuthActivity], clearing the Custom Tab off the back stack.
 */
class LogtoRedirectReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val redirectUri = intent.data
        // This activity is exported and its intent filter only matches the scheme,
        // so any app can send it an arbitrary URI. Forward only the redirect the
        // pending session is waiting for — a forged intent must not be able to
        // disturb an in-flight flow.
        if (redirectUri != null && LogtoAuthManager.isLogtoAuthResult(redirectUri)) {
            startActivity(LogtoBrowserAuthActivity.createRedirectHandlingIntent(this, redirectUri))
        }
        finish()
    }
}
