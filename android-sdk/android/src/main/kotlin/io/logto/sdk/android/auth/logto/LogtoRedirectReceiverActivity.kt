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
        startActivity(LogtoBrowserAuthActivity.createRedirectHandlingIntent(this, intent.data))
        finish()
    }
}
