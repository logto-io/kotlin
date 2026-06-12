package io.logto.sdk.android.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PersistStorageTest {

    @Test
    fun `should set and get and remove data correctly`() {
        val storageName = "StorageName"
        val context: Context = ApplicationProvider.getApplicationContext()
        val persistStorage = PersistStorage(context, storageName)
        val dataKey = "dataKey"
        val dataValue = "value"

        persistStorage.setItem(dataKey, dataValue)
        val retrievedData = persistStorage.getItem(dataKey)
        assertThat(retrievedData).isEqualTo(dataValue)

        persistStorage.setItem(dataKey, null)
        val retrievedDataAfterRemove = persistStorage.getItem(dataKey)
        assertThat(retrievedDataAfterRemove).isNull()
    }
}
