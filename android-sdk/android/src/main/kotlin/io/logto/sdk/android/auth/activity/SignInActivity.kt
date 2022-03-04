package io.logto.sdk.android.auth.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import io.logto.sdk.android.auth.session.SignInSessionManager

class SignInActivity : AppCompatActivity() {

    private var isSigningIn: Boolean = false

    private val customTabsAvailable: Boolean by lazy {
        isCustomTabsAvailable()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        processSignIn()
    }

    private fun processSignIn() {
        val redirectComplete = requireNotNull(intent.getBooleanExtra(EXTRA_REDIRECT_COMPLETE, false))
        if (redirectComplete) {
            SignInSessionManager.clearSession()
            isSigningIn = false
            finish()
            return
        }

        if (!isSigningIn) {
            startSignIn()
            isSigningIn = true
        } else {
            SignInSessionManager.handleUserCancel()
            isSigningIn = false
            finish()
        }
    }

    private fun startSignIn() {
        val signInUri = requireNotNull(intent.getStringExtra(EXTRA_SIGN_IN_URI))
        val redirectUri = requireNotNull(intent.getStringExtra(EXTRA_REDIRECT_URI))

        // Notes: If we don't use custom scheme as our redirect uri scheme, custom tabs will
        // not open the external app in some circumstances, so we should fall back to browser
        // https://bugs.chromium.org/p/chromium/issues/detail?id=536037
        if (redirectUri.startsWith("http") || !customTabsAvailable) {
            signInWithBrowser(signInUri)
        } else {
            signInWithCustomTabs(signInUri)
        }
    }

    private fun signInWithBrowser(signInUri: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(signInUri)))
    }

    private fun signInWithCustomTabs(signInUri: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, Uri.parse(signInUri))
    }

    // Notes: https://developer.chrome.com/docs/android/custom-tabs/integration-guide/#how-can-i-check-whether-the-android-device-has-a-browser-that-supports-custom-tab
    private fun isCustomTabsAvailable(): Boolean {
        val activityIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.fromParts("http", "", null)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        val resolvedActivityList = packageManager.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = mutableListOf<ResolveInfo>()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent().apply {
                action = ACTION_CUSTOM_TABS_CONNECTION
                setPackage(info.activityInfo.packageName)
            }
            packageManager.resolveService(serviceIntent, 0)?.let {
                packagesSupportingCustomTabs.add(info)
            }
        }

        return packagesSupportingCustomTabs.isNotEmpty()
    }

    companion object {
        private const val EXTRA_SIGN_IN_URI = "EXTRA_SIGN_IN_URI"
        private const val EXTRA_REDIRECT_URI = "EXTRA_REDIRECT_URI"
        private const val EXTRA_REDIRECT_COMPLETE = "EXTRA_REDIRECT_COMPLETE"

        fun createIntent(
            context: Activity,
            signInUri: String,
            redirectUri: String,
        ) = Intent(context, SignInActivity::class.java).apply {
            putExtra(EXTRA_SIGN_IN_URI, signInUri)
            putExtra(EXTRA_REDIRECT_URI, redirectUri)
            putExtra(EXTRA_REDIRECT_COMPLETE, false)
        }

        // MARK: tell SignInActivity that a redirect action is completed.
        fun createRedirectCompleteIntent(context: Activity) = Intent(context, SignInActivity::class.java).apply {
            putExtra(EXTRA_REDIRECT_COMPLETE, true)
        }
    }
}
