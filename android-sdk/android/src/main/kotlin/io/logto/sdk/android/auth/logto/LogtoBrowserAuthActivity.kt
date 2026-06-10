package io.logto.sdk.android.auth.logto

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import io.logto.sdk.android.exception.LogtoException

/**
 * Manages the browser round-trip of an authentication flow.
 *
 * This activity stays beneath the Custom Tab it launches:
 * - When the redirect arrives, [LogtoRedirectReceiverActivity] re-delivers it here via
 *   [onNewIntent] and the callback URI is dispatched to the pending session.
 * - When the user dismisses the Custom Tab without completing the flow, this activity
 *   resumes without a redirect, which is reported as a user cancellation.
 */
class LogtoBrowserAuthActivity : Activity() {
    private var authStarted = false
    private var authUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            authUri = intent.getStringExtra(EXTRA_AUTH_URI)?.let(Uri::parse)
        } else {
            authStarted = savedInstanceState.getBoolean(KEY_AUTH_STARTED, false)
            authUri = savedInstanceState.getString(EXTRA_AUTH_URI)?.let(Uri::parse)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let(::setIntent)
    }

    override fun onResume() {
        super.onResume()

        // Handle a delivered redirect before consulting [authStarted]: if the system
        // destroyed this activity while the Custom Tab was in the foreground, the
        // redirect re-creates it with a fresh instance state.
        val callbackUri = intent.data
        if (callbackUri != null) {
            if (LogtoAuthManager.isLogtoAuthResult(callbackUri)) {
                LogtoAuthManager.handleCallbackUri(callbackUri)
            }
            // A URI that does not belong to the pending session can neither complete
            // nor cancel it. [LogtoRedirectReceiverActivity] already filters these
            // out; this is defense in depth.
            finish()
            return
        }

        if (!authStarted) {
            startBrowserAuth()
            return
        }

        // Resumed without a redirect: the user dismissed the Custom Tab.
        LogtoAuthManager.handleUserCancel()
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_AUTH_STARTED, authStarted)
        outState.putString(EXTRA_AUTH_URI, authUri?.toString())
    }

    private fun startBrowserAuth() {
        val uri = authUri
        if (uri == null) {
            // Reachable only if the activity is launched without [EXTRA_AUTH_URI];
            // still report a failure so a pending session is never left hanging.
            LogtoAuthManager.handleFailure(
                LogtoException(LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER).apply {
                    detail = "Missing auth URI."
                },
            )
            finish()
            return
        }

        authStarted = true
        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            // Sign-in URLs carry one-time state / PKCE parameters and must not be shared.
            // androidx.browser 1.3.0 predates `setShareState`, so set the extra directly.
            customTabsIntent.intent.putExtra(EXTRA_SHARE_STATE, SHARE_STATE_OFF)
            customTabsIntent.launchUrl(this, uri)
        } catch (exception: ActivityNotFoundException) {
            LogtoAuthManager.handleFailure(
                LogtoException(LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER, exception),
            )
            finish()
        }
    }

    companion object {
        private const val EXTRA_AUTH_URI = "EXTRA_AUTH_URI"
        private const val KEY_AUTH_STARTED = "KEY_AUTH_STARTED"

        // `CustomTabsIntent.EXTRA_SHARE_STATE` / `SHARE_STATE_OFF` from androidx.browser 1.4.0.
        private const val EXTRA_SHARE_STATE = "androidx.browser.customtabs.extra.SHARE_STATE"
        private const val SHARE_STATE_OFF = 2

        fun launch(context: Activity, uri: String) {
            context.startActivity(
                Intent(context, LogtoBrowserAuthActivity::class.java).apply {
                    putExtra(EXTRA_AUTH_URI, uri)
                },
            )
        }

        internal fun createRedirectHandlingIntent(context: Context, redirectUri: Uri?): Intent =
            Intent(context, LogtoBrowserAuthActivity::class.java).apply {
                data = redirectUri
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
    }
}
