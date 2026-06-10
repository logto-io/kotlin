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

        if (!authStarted) {
            startBrowserAuth()
            return
        }

        val callbackUri = intent.data
        if (callbackUri != null && LogtoAuthManager.isLogtoAuthResult(callbackUri)) {
            LogtoAuthManager.handleCallbackUri(callbackUri)
        } else {
            LogtoAuthManager.handleUserCancel()
        }
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
            finish()
            return
        }

        authStarted = true
        try {
            CustomTabsIntent.Builder().build().launchUrl(this, uri)
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
