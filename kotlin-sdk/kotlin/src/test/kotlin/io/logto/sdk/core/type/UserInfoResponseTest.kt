package io.logto.sdk.core.type

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserInfoResponseTest {
    private val sub = "testSub"
    private val userInfoResponse = UserInfoResponse(sub = sub)

    @Test
    fun `RefreshTokenTokenResponse should get expected sub`() {
        assertThat(userInfoResponse.sub).isEqualTo(sub)
    }
}
