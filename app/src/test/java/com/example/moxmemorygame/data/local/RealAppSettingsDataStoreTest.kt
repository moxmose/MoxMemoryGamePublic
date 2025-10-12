package com.example.moxmemorygame.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.data.local.RealAppSettingsDataStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RealAppSettingsDataStoreTest {

    @get:Rule
    val testName = TestName()

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(UnconfinedTestDispatcher())
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var dataStore: IAppSettingsDataStore

    private fun getTestFile() = testContext.preferencesDataStoreFile(testName.methodName)

    @Before
    fun setUp() {
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = { getTestFile() }
        )
        dataStore = RealAppSettingsDataStore(testDataStore, testCoroutineScope)
    }

    @After
    fun tearDown() {
        getTestFile().delete()
        testCoroutineScope.coroutineContext[Job]?.cancel()
        stopKoin()
    }

    @Test
    fun `save and retrieve player name`() = runTest {
        val playerName = "Mox"
        dataStore.savePlayerName(playerName)
        val retrievedName = dataStore.playerName.first()
        assertThat(retrievedName).isEqualTo(playerName)
    }

    @Test
    fun `save and retrieve board dimensions`() = runTest {
        val width = 5
        val height = 6
        dataStore.saveBoardDimensions(width, height)
        val retrievedWidth = dataStore.selectedBoardWidth.first()
        val retrievedHeight = dataStore.selectedBoardHeight.first()
        assertThat(retrievedWidth).isEqualTo(width)
        assertThat(retrievedHeight).isEqualTo(height)
    }

    @Test
    fun `save and retrieve selected cards`() = runTest {
        val cards = setOf("img_c_01", "img_c_05", "img_c_10")
        dataStore.saveSelectedCards(cards)
        val retrievedCards = dataStore.selectedCards.first()
        assertThat(retrievedCards).isEqualTo(cards)
    }

    @Test
    fun `save and retrieve selected backgrounds`() = runTest {
        val backgrounds = setOf("background_00", "background_03")
        dataStore.saveSelectedBackgrounds(backgrounds)
        val retrievedBackgrounds = dataStore.selectedBackgrounds.first()
        assertThat(retrievedBackgrounds).isEqualTo(backgrounds)
    }
}