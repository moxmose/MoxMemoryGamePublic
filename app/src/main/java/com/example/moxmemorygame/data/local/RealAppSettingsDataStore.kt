package com.example.moxmemorygame.data.local // Pacchetto aggiornato

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.moxmemorygame.model.ScoreEntry
// L'import di IAppSettingsDataStore verrà aggiornato automaticamente se necessario,
// o era già esplicito e corretto se IAppSettingsDataStore era in un package diverso.
// Se IAppSettingsDataStore era nello stesso package radice, l'import non era necessario
// ma ora lo diventerà.
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

// Questa definizione top-level si sposta con il file e apparterrà al package com.example.moxmemorygame.data.local
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RealAppSettingsDataStore(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : IAppSettingsDataStore { // L'interfaccia IAppSettingsDataStore verrà risolta dal nuovo package

    // Mutex per sincronizzare gli accessi in scrittura a topRanking
    private val rankingMutex = Mutex()

    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val SELECTED_BACKGROUNDS_KEY = stringSetPreferencesKey("selected_backgrounds")
        val SELECTED_CARDS_KEY = stringSetPreferencesKey("selected_cards")
        val TOP_RANKING_KEY = stringPreferencesKey("top_ranking")
        val LAST_PLAYED_ENTRY_KEY = stringPreferencesKey("last_played_entry")

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

    override val playerName: StateFlow<String> = playerNameSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_PLAYER_NAME)
    override val selectedBackgrounds: StateFlow<Set<String>> = selectedBackgroundsSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_SELECTED_BACKGROUNDS)
    override val selectedCards: StateFlow<Set<String>> = selectedCardsSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_SELECTED_CARDS)
    override val topRanking: StateFlow<List<ScoreEntry>> = topRankingSourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_TOP_RANKING)
    override val lastPlayedEntry: StateFlow<ScoreEntry?> = lastPlayedEntrySourceFlow.stateIn(externalScope, SharingStarted.WhileSubscribed(5000), DEFAULT_LAST_PLAYED_ENTRY)

    init {
        externalScope.launch {
            Log.d("DataStore", "Initializing RealAppSettingsDataStore - waiting for shared StateFlows to emit their first value...")
            playerName.first()
            selectedBackgrounds.first()
            selectedCards.first()
            topRanking.first()
            lastPlayedEntry.first()
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

        // Salva come lastPlayedEntry
        dataStore.edit {
            val jsonLastPlayed = json.encodeToString(newEntry)
            it[LAST_PLAYED_ENTRY_KEY] = jsonLastPlayed
            Log.d("DataStore", "Saved lastPlayedEntry JSON: $jsonLastPlayed")
        }

        // Aggiorna topRanking in modo sincronizzato
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
}