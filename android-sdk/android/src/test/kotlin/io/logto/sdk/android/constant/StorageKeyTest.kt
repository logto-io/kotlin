package io.logto.sdk.android.constant

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StorageKeyTest {
    @Test
    fun `Storage key constant should not be null`() {
        assertThat(StorageKey).isNotNull()
    }
}
