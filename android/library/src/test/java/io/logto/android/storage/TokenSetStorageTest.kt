package io.logto.android.storage

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.logto.android.config.LogtoConfig
import io.logto.android.constant.ScopeValue
import io.logto.android.model.TokenSet
import io.logto.android.model.TokenSetParameters
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TokenSetStorageTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    private val logtoConfig = LogtoConfig(
        domain = "logto.dev",
        clientId = "clientId",
        scopes = listOf(ScopeValue.OPEN_ID, ScopeValue.OFFLINE_ACCESS),
        redirectUri = "redirectUri",
        postLogoutRedirectUri = "postLogoutRedirectUri",
    )

    private val testTokenSet = TokenSet(TokenSetParameters(
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        idToken = "idToken",
        scope = "offline_access openid",
        tokenType = "Bearer",
        expiresIn = 60L
    ))

    private val sharedPreferencesName = "io.logto.android::${logtoConfig.cacheKey}"

    private val tokenSetStorage = TokenSetStorage(context, sharedPreferencesName)

    @Test
    fun tokenSetShouldSaveAndGetAndClearCorrectly() {
        tokenSetStorage.tokenSet = testTokenSet
        val storedTokenSet = tokenSetStorage.tokenSet
        assertThat(storedTokenSet).isEqualTo(testTokenSet)
        tokenSetStorage.tokenSet = null
        val tokenSetAfterCleared = tokenSetStorage.tokenSet
        assertThat(tokenSetAfterCleared).isNull()
    }
}
