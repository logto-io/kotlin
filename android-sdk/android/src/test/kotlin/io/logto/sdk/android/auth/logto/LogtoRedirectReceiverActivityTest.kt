package io.logto.sdk.android.auth.logto

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class LogtoRedirectReceiverActivityTest {

    @Test
    fun `should forward the redirect uri to the browser auth activity and finish`() {
        val callbackUri = Uri.parse("io.logto.android://io.logto.sample/callback?code=code&state=state")
        val appContext: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(Intent.ACTION_VIEW, callbackUri, appContext, LogtoRedirectReceiverActivity::class.java)

        val activity = Robolectric.buildActivity(LogtoRedirectReceiverActivity::class.java, intent).create().get()

        val startedIntent = shadowOf(activity).nextStartedActivity
        assertThat(startedIntent.component?.className).isEqualTo(LogtoBrowserAuthActivity::class.java.name)
        assertThat(startedIntent.data).isEqualTo(callbackUri)
        assertThat(startedIntent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP).isNotEqualTo(0)
        assertThat(startedIntent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP).isNotEqualTo(0)
        assertThat(activity.isFinishing).isTrue()
    }
}
