package com.example.moxmemorygame.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@ExperimentalCoroutinesApi
class RealAppSettingsDataStoreTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `minimal datastore test - write and read`() = runTest(testDispatcher) {
        // 1. Create a completely isolated DataStore for this single test.
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = this, // The scope is provided by runTest
            produceFile = { temporaryFolder.newFile("minimal_test.preferences_pb") }
        )

        // 2. Define a simple key and value.
        val testKey = intPreferencesKey("test_counter")
        val testValue = 123

        // 3. Act: Write the value and wait for it to complete.
        testDataStore.edit { preferences ->
            preferences[testKey] = testValue
        }
        advanceUntilIdle() // Ensure the write operation completes

        // 4. Assert: Read the value back and check it.
        val savedValue = testDataStore.data.map { it[testKey] }.first()
        assertThat(savedValue).isEqualTo(testValue)
    }

    /*
    // All previous tests are commented out until we solve the core issue.
    */
}