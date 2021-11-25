package io.logto.android.auth.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import io.logto.android.auth.AuthManager

class AuthorizationActivity : AppCompatActivity() {

    private val customTabsAvailable: Boolean
        get() {
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

    private var flowStarted: Boolean = false

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleFlow()
    }

    private fun handleFlow() {
        if (flowStarted) {
            val flowComplete = intent.getBooleanExtra(EXTRA_FLOW_COMPLETE_FLAG, false)
            if (!flowComplete) {
                AuthManager.handleUserCanceled()
            }
            handleFlowEnd()
            return
        }

        val endpoint = intent.getStringExtra(EXTRA_FLOW_ENDPOINT)
        if (endpoint == null) {
            handleFlowEnd()
            return
        }

        handleFlowStart(endpoint)
    }

    private fun handleFlowStart(endpoint: String) {
        val redirectUri = intent.getStringExtra(EXTRA_FLOW_REDIRECT_URI)
        if (redirectUri?.startsWith("http") == true ||
            !customTabsAvailable
        ) {
            startFlowWithBrowser(endpoint)
            flowStarted = true
            return
        }

        startFlowWithCustomTabs(endpoint)
        flowStarted = true
    }

    private fun handleFlowEnd() {
        finish()
        flowStarted = false
    }

    private fun startFlowWithCustomTabs(endpoint: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, Uri.parse(endpoint))
    }

    private fun startFlowWithBrowser(endpoint: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(endpoint)))
    }

    companion object {
        private const val EXTRA_FLOW_ENDPOINT = "EXTRA_FLOW_ENDPOINT"
        private const val EXTRA_FLOW_REDIRECT_URI = "EXTRA_FLOW_REDIRECT_URI"
        private const val EXTRA_FLOW_COMPLETE_FLAG = "EXTRA_FLOW_COMPLETE_FLAG"

        fun createHandleStartIntent(
            context: Context,
            endpoint: String,
            redirectUri: String,
        ): Intent {
            return Intent(context, AuthorizationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_FLOW_ENDPOINT, endpoint)
                putExtra(EXTRA_FLOW_REDIRECT_URI, redirectUri)
                putExtra(EXTRA_FLOW_COMPLETE_FLAG, false)
            }
        }

        fun createHandleCompleteIntent(
            context: Context,
        ): Intent {
            return Intent(context, AuthorizationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_FLOW_COMPLETE_FLAG, true)
            }
        }
    }
}
