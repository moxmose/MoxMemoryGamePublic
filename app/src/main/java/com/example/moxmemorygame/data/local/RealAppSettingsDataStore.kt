package com.example.moxmemorygame.data.local // Pacchetto aggiornato

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey // Import per intPreferencesKey
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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RealAppSettingsDataStore(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : IAppSettingsDataStore {

    private val rankingMutex = Mutex()

    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val SELECTED_BACKGROUNDS_KEY = stringSetPreferencesKey("selected_backgrounds")
        val SELECTED_CARDS_KEY = stringSetPreferencesKey("selected_cards")
        val TOP_RANKING_KEY = stringPreferencesKey("top_ranking")
        val LAST_PLAYED_ENTRY_KEY = stringPreferencesKey("last_played_entry")
        // NUOVE CHIAVI
        val BOARD_WIDTH_KEY = intPreferencesKey("board_width")
        val BOARD_HEIGHT_KEY = intPreferencesKey("board_height")

        const val DEFAULT_PLAYER_NAME = "Default Player"
        val DEFAULT_SELECTED_BACKGROUNDS = setOf("background_00")
        val DEFAULT_SELECTED_CARDS = setOf(
            "img_c_00", "img_c_01", "img_c_02", "img_c_03", "img_c_04",
            "img_c_05", "img_c_06", "img_c_07", "img_c_08", "img_c_09",
            "img_c_10", "img_c_11", "img_c_12", "img_c_13", "img_c_14",
            "img_c_15", "img_c_16", "img_c_17", "img_c_18", "img_c_19"
        )
        val DEFAULT_TOP_RANKING = emptyList<ScoreEntry>()
        val DEFAULT_LAST_PLAYED_ENTRY = null
        // NUOVI VALORI DI DEFAULT
        const val DEFAULT_BOARD_WIDTH = 4
        const val DEFAULT_BOARD_HEIGHT = 5 // (4x5 = 20 carte, 10 coppie)
    }

    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _isDataLoaded = MutableStateFlow(false)
    override val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    private val playerNameSourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PLAYER_NAME_KEY] ?: DEFAULT_PLAYER_NAME }

    private val selectedBackgroundsSourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[SELECTED_BACKGROUNDS_KEY]?.takeIf { it.isNotEmpty() } ?: DEFAULT_SELECTED_BACKGROUNDS
        }

    private val selectedCardsSourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[SELECTED_CARDS_KEY]?.takeIf { it.isNotEmpty() } ?: DEFAULT_SELECTED_CARDS
        }

    private val topRankingSourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[TOP_RANKING_KEY]?.let {
                try { json.decodeFromString<List<ScoreEntry>>(it) } catch (e: Exception) { 
                    Log.e("DataStore_Map", "Error decoding topRanking: ${e.message}", e)
                    DEFAULT_TOP_RANKING 
                }
            } ?: DEFAULT_TOP_RANKING
        }

    private val lastPlayedEntrySourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[LAST_PLAYED_ENTRY_KEY]?.let {
                try { json.decodeFromString<ScoreEntry?>(it) } catch (e: Exception) { 
                    Log.e("DataStore_Map", "Error decoding lastPlayedEntry: ${e.message}", e)
                    DEFAULT_LAST_PLAYED_ENTRY 
                }
            } ?: DEFAULT_LAST_PLAYED_ENTRY
        }
    // NUOVI FLUSSI SORGENTE
    private val selectedBoardWidthSourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[BOARD_WIDTH_KEY] ?: DEFAULT_BOARD_WIDTH }

    private val selectedBoardHeightSourceFlow = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[BOARD_HEIGHT_KEY] ?: DEFAULT_BOARD_HEIGHT }

    override val playerName: StateFlow<String> = playerNameSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_PLAYER_NAME)
    override val selectedBackgrounds: StateFlow<Set<String>> = selectedBackgroundsSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_SELECTED_BACKGROUNDS)
    override val selectedCards: StateFlow<Set<String>> = selectedCardsSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_SELECTED_CARDS)
    override val topRanking: StateFlow<List<ScoreEntry>> = topRankingSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_TOP_RANKING)
    override val lastPlayedEntry: StateFlow<ScoreEntry?> = lastPlayedEntrySourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_LAST_PLAYED_ENTRY)
    // NUOVE IMPLEMENTAZIONI STATEFLOW
    override val selectedBoardWidth: StateFlow<Int> = selectedBoardWidthSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_BOARD_WIDTH)
    override val selectedBoardHeight: StateFlow<Int> = selectedBoardHeightSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_BOARD_HEIGHT)

    init {
        externalScope.launch {
            Log.d("DataStore", "Initializing RealAppSettingsDataStore - waiting for shared StateFlows to emit their first value...")
            playerName.first()
            selectedBackgrounds.first()
            selectedCards.first()
            topRanking.first()
            lastPlayedEntry.first()
            // AGGIUNTA LETTURA INIZIALE
            selectedBoardWidth.first()
            selectedBoardHeight.first()
            Log.d("DataStore", "All shared StateFlows have emitted. Setting isDataLoaded to true.")
            _isDataLoaded.value = true
        }
    }

    override suspend fun savePlayerName(name: String) {
        dataStore.edit { it[PLAYER_NAME_KEY] = name }
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        dataStore.edit { it[SELECTED_BACKGROUNDS_KEY] = backgrounds }
    }

    override suspend fun saveSelectedCards(selectedCardsToSave: Set<String>) {
        dataStore.edit { it[SELECTED_CARDS_KEY] = selectedCardsToSave }
    }

    override suspend fun saveScore(playerName: String, score: Int) {
        val newEntry = ScoreEntry(playerName = playerName, score = score, timestamp = System.currentTimeMillis())
        Log.d("DataStore", "Saving new score: $newEntry")

        dataStore.edit {
            val jsonLastPlayed = json.encodeToString(newEntry)
            it[LAST_PLAYED_ENTRY_KEY] = jsonLastPlayed
            Log.d("DataStore", "Saved lastPlayedEntry JSON: $jsonLastPlayed")
        }

        rankingMutex.withLock {
            val currentRankingJson = dataStore.data.first()[TOP_RANKING_KEY]
            val currentRanking = currentRankingJson?.let {
                try { json.decodeFromString<List<ScoreEntry>>(it) } catch (e: Exception) { emptyList() }
            } ?: emptyList()
            
            Log.d("DataStore", "Current topRanking before adding new score: $currentRanking")

            val updatedRanking = (currentRanking + newEntry)
                .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
                .take(ScoreEntry.MAX_RANKING_ENTRIES)
            
            Log.d("DataStore", "Updated topRanking: $updatedRanking")

            dataStore.edit {
                val jsonTopRanking = json.encodeToString(updatedRanking)
                it[TOP_RANKING_KEY] = jsonTopRanking
                Log.d("DataStore", "Saved topRanking JSON: $jsonTopRanking")
            }
        }
    }

    // NUOVA FUNZIONE DI SALVATAGGIO
    override suspend fun saveBoardDimensions(width: Int, height: Int) {
        dataStore.edit {
            it[BOARD_WIDTH_KEY] = width
            it[BOARD_HEIGHT_KEY] = height
        }
    }
}
