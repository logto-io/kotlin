package io.logto.sdk.core.constant

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConstantTest {
    @Test
    fun `all constants should not be null`() {
        assertThat(ClaimName).isNotNull()
        assertThat(CodeChallengeMethod).isNotNull()
        assertThat(GrantType).isNotNull()
        assertThat(PromptValue).isNotNull()
        assertThat(QueryKey).isNotNull()
        assertThat(ReservedScope).isNotNull()
        assertThat(ReservedResource).isNotNull()
        assertThat(UserScope).isNotNull()
        assertThat(ResponseType).isNotNull()
    }
}
