package com.example.moxmemorygame.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.moxmemorygame.model.ScoreEntry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RealAppSettingsDataStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var testScope: TestScope
    private lateinit var testContext: Context
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var appSettingsDataStore: RealAppSettingsDataStore

    private val testDatastoreName = "test_datastore"

    private object Keys {
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val SELECTED_CARDS = stringSetPreferencesKey("selected_cards")
        val SELECTED_BACKGROUNDS = stringSetPreferencesKey("selected_backgrounds")
        val SELECTED_BOARD_WIDTH = intPreferencesKey("selected_board_width")
        val SELECTED_BOARD_HEIGHT = intPreferencesKey("selected_board_height")
        val IS_FIRST_TIME_LAUNCH = booleanPreferencesKey("is_first_time_launch")
        val TOP_RANKING = stringPreferencesKey("top_ranking")
        val LAST_PLAYED_ENTRY = stringPreferencesKey("last_played_entry")
    }

    @Before
    fun setup() {
        testScope = TestScope(testDispatcher + Job())
        testContext = ApplicationProvider.getApplicationContext()
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile(testDatastoreName) }
        )
        appSettingsDataStore = RealAppSettingsDataStore(testDataStore)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        stopKoin() // Stop Koin to avoid conflicts between test classes
        val datastoreFile = testContext.filesDir.resolve("datastore/$testDatastoreName.preferences_pb")
        if (datastoreFile.exists()) {
            datastoreFile.delete()
        }
    }

    @Test
    fun savePlayerName_savesNameCorrectly() = testScope.runTest {
        val playerName = "TestPlayer"

        appSettingsDataStore.savePlayerName(playerName)
        advanceUntilIdle()

        val savedName = testDataStore.data.map { it[Keys.PLAYER_NAME] }.first()
        assertThat(savedName).isEqualTo(playerName)
    }

    @Test
    fun saveSelectedCards_savesCardSetCorrectly() = testScope.runTest {
        val cards = setOf("img_c_01", "img_c_02", "img_s_05")

        appSettingsDataStore.saveSelectedCards(cards)
        advanceUntilIdle()

        val savedCards = testDataStore.data.map { it[Keys.SELECTED_CARDS] }.first()
        assertThat(savedCards).isEqualTo(cards)
    }

    @Test
    fun saveSelectedBackgrounds_savesBackgroundSetCorrectly() = testScope.runTest {
        val backgrounds = setOf("background_01", "background_03")

        appSettingsDataStore.saveSelectedBackgrounds(backgrounds)
        advanceUntilIdle()

        val savedBackgrounds = testDataStore.data.map { it[Keys.SELECTED_BACKGROUNDS] }.first()
        assertThat(savedBackgrounds).isEqualTo(backgrounds)
    }

    @Test
    fun saveBoardDimensions_savesWidthAndHeightCorrectly() = testScope.runTest {
        val width = 4
        val height = 5

        appSettingsDataStore.saveBoardDimensions(width, height)
        advanceUntilIdle()

        val savedWidth = testDataStore.data.map { it[Keys.SELECTED_BOARD_WIDTH] }.first()
        val savedHeight = testDataStore.data.map { it[Keys.SELECTED_BOARD_HEIGHT] }.first()
        assertThat(savedWidth).isEqualTo(width)
        assertThat(savedHeight).isEqualTo(height)
    }

    @Test
    fun saveIsFirstTimeLaunch_savesBooleanFlagCorrectly() = testScope.runTest {
        val isFirstTime = false

        appSettingsDataStore.saveIsFirstTimeLaunch(isFirstTime)
        advanceUntilIdle()

        val savedFlag = testDataStore.data.map { it[Keys.IS_FIRST_TIME_LAUNCH] }.first()
        assertThat(savedFlag).isEqualTo(isFirstTime)
    }

    @Test
    fun saveScore_savesLastEntryAndUpdatesRanking() = testScope.runTest {
        val player1 = "Player1"
        val score1 = 100
        val player2 = "Player2"
        val score2 = 200

        // Act: Save first score
        appSettingsDataStore.saveScore(player1, score1)
        advanceUntilIdle()

        // Assert: Check first save
        val lastEntryJson1 = testDataStore.data.map { it[Keys.LAST_PLAYED_ENTRY] }.first()
        val rankingJson1 = testDataStore.data.map { it[Keys.TOP_RANKING] }.first()
        val lastEntry1 = Json.decodeFromString<ScoreEntry>(lastEntryJson1!!)
        val ranking1 = Json.decodeFromString<List<ScoreEntry>>(rankingJson1!!)

        assertThat(lastEntry1.playerName).isEqualTo(player1)
        assertThat(lastEntry1.score).isEqualTo(score1)
        assertThat(ranking1).hasSize(1)

        // Act: Save second, higher score
        appSettingsDataStore.saveScore(player2, score2)
        advanceUntilIdle()

        // Assert: Check second save and ranking update
        val lastEntryJson2 = testDataStore.data.map { it[Keys.LAST_PLAYED_ENTRY] }.first()
        val rankingJson2 = testDataStore.data.map { it[Keys.TOP_RANKING] }.first()
        val lastEntry2 = Json.decodeFromString<ScoreEntry>(lastEntryJson2!!)
        val ranking2 = Json.decodeFromString<List<ScoreEntry>>(rankingJson2!!)

        assertThat(lastEntry2.playerName).isEqualTo(player2)
        assertThat(lastEntry2.score).isEqualTo(score2)
        assertThat(ranking2).hasSize(2)
        assertThat(ranking2.first().score).isEqualTo(score2) // Verify correct sorting (highest score first)
    }
}