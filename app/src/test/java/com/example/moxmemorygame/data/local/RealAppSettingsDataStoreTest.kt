package com.example.moxmemorygame.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Test diagnostico per isolare il problema di DataStore.
 * Questo test usa Robolectric per sfruttare il suo Context e file system, 
 * eliminando le dipendenze dal test runner JVM puro e da TemporaryFolder.
 */
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RealAppSettingsDataStoreTest {

    private lateinit var context: Context
    private val TEST_DATASTORE_NAME = "diagnostic_test_datastore"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        // Pulisce manualmente il file di DataStore dopo ogni test
        val datastoreFile = context.filesDir.resolve("datastore/$TEST_DATASTORE_NAME.preferences_pb")
        if (datastoreFile.exists()) {
            datastoreFile.delete()
        }
    }

    @Test
    fun `datastore test using Robolectric context`() = runTest {
        // Arrange: Crea un DataStore usando il file system del contesto Android.
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = this,
            produceFile = { context.preferencesDataStoreFile(TEST_DATASTORE_NAME) }
        )
        val testKey = intPreferencesKey("test_counter")
        val testValue = 123

        // Act
        testDataStore.edit { preferences ->
            preferences[testKey] = testValue
        }

        // Advance: Esegui le coroutine in background.
        advanceUntilIdle()

        // Assert
        val savedValue = testDataStore.data.map { it[testKey] }.first()
        assertThat(savedValue).isEqualTo(testValue)
    }
}