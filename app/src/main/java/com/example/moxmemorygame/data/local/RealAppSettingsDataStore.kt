package com.example.moxmemorygame.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

// La creazione del DataStore viene lasciata al dependency injector (es. Koin)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class RealAppSettingsDataStore(
    private val dataStore: DataStore<Preferences>,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : IAppSettingsDataStore {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val rankingMutex = Mutex()

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

    private val _isDataLoaded = MutableStateFlow(false)
    override val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    override val playerName: StateFlow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PLAYER_NAME] ?: IAppSettingsDataStore.DEFAULT_PLAYER_NAME }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), IAppSettingsDataStore.DEFAULT_PLAYER_NAME)

    override val selectedCards: StateFlow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SELECTED_CARDS] ?: IAppSettingsDataStore.DEFAULT_SELECTED_CARDS }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), IAppSettingsDataStore.DEFAULT_SELECTED_CARDS)

    override val selectedBackgrounds: StateFlow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SELECTED_BACKGROUNDS] ?: IAppSettingsDataStore.DEFAULT_SELECTED_BACKGROUNDS }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), IAppSettingsDataStore.DEFAULT_SELECTED_BACKGROUNDS)

    override val selectedBoardWidth: StateFlow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SELECTED_BOARD_WIDTH] ?: IAppSettingsDataStore.DEFAULT_BOARD_WIDTH }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), IAppSettingsDataStore.DEFAULT_BOARD_WIDTH)

    override val selectedBoardHeight: StateFlow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SELECTED_BOARD_HEIGHT] ?: IAppSettingsDataStore.DEFAULT_BOARD_HEIGHT }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), IAppSettingsDataStore.DEFAULT_BOARD_HEIGHT)

    override val isFirstTimeLaunch: StateFlow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.IS_FIRST_TIME_LAUNCH] ?: true }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), true)

    override val topRanking: StateFlow<List<ScoreEntry>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[Keys.TOP_RANKING]?.let { json.decodeFromString<List<ScoreEntry>>(it) } ?: emptyList()
        }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override val lastPlayedEntry: StateFlow<ScoreEntry?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[Keys.LAST_PLAYED_ENTRY]?.let { json.decodeFromString<ScoreEntry?>(it) }
        }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        externalScope.launch {
            isFirstTimeLaunch.first()
            _isDataLoaded.value = true
        }
    }

    override suspend fun savePlayerName(name: String) {
        dataStore.edit { it[Keys.PLAYER_NAME] = name }
    }

    override suspend fun saveSelectedCards(cards: Set<String>) {
        dataStore.edit { it[Keys.SELECTED_CARDS] = cards }
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        dataStore.edit { it[Keys.SELECTED_BACKGROUNDS] = backgrounds }
    }

    override suspend fun saveBoardDimensions(width: Int, height: Int) {
        dataStore.edit {
            it[Keys.SELECTED_BOARD_WIDTH] = width
            it[Keys.SELECTED_BOARD_HEIGHT] = height
        }
    }

    override suspend fun saveIsFirstTimeLaunch(isFirstTime: Boolean) {
        dataStore.edit { it[Keys.IS_FIRST_TIME_LAUNCH] = isFirstTime }
    }

    override suspend fun saveScore(playerName: String, score: Int) {
        val newEntry = ScoreEntry(playerName, score, System.currentTimeMillis())
        dataStore.edit { it[Keys.LAST_PLAYED_ENTRY] = json.encodeToString(newEntry) }

        rankingMutex.withLock {
            val currentRanking = topRanking.first()
            val updatedRanking = (currentRanking + newEntry)
                .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
                .take(ScoreEntry.MAX_RANKING_ENTRIES)
            dataStore.edit { it[Keys.TOP_RANKING] = json.encodeToString(updatedRanking) }
        }
    }
}