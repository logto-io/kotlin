package io.logto.sdk.android.type

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.util.TokenUtils
import org.junit.Assert
import org.junit.Test

class IdTokenVerificationOptionsTest {
    @Test
    fun `IdTokenVerificationOptions's clockTolerance should default to the SDK default`() {
        assertThat(IdTokenVerificationOptions().clockTolerance)
            .isEqualTo(TokenUtils.DEFAULT_CLOCK_TOLERANCE_IN_SECONDS)
    }

    @Test
    fun `IdTokenVerificationOptions should keep the custom clockTolerance`() {
        assertThat(IdTokenVerificationOptions(clockTolerance = 600).clockTolerance).isEqualTo(600)
    }

    @Test
    fun `IdTokenVerificationOptions should throw with non-positive clockTolerance`() {
        for (clockTolerance in listOf(0, -1)) {
            Assert.assertThrows(IllegalArgumentException::class.java) {
                IdTokenVerificationOptions(clockTolerance = clockTolerance)
            }
        }
    }
}
