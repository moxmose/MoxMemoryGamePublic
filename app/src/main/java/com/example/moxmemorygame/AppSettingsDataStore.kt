package com.example.moxmemorygame

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RealAppSettingsDataStore(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : IAppSettingsDataStore {

    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val SELECTED_BACKGROUNDS_KEY = stringSetPreferencesKey("selected_backgrounds")
        val SELECTED_CARDS_KEY = stringSetPreferencesKey("selected_cards")

        const val DEFAULT_PLAYER_NAME = "Default Player"
        val DEFAULT_SELECTED_BACKGROUNDS = setOf("background_00")
        val DEFAULT_SELECTED_CARDS = setOf(
            "img_c_00", "img_c_01", "img_c_02", "img_c_03", "img_c_04",
            "img_c_05", "img_c_06", "img_c_07", "img_c_08", "img_c_09",
            "img_c_10", "img_c_11", "img_c_12", "img_c_13", "img_c_14",
            "img_c_15", "img_c_16", "img_c_17", "img_c_18", "img_c_19"
        )
        // Proposta di valore sentinella se decidiamo di usarlo, per ora non implementato attivamente
        // val UNINITIALIZED_CARDS_SENTINEL = setOf("___UNINITIALIZED_CARD_SENTINEL_VALUE___") 
    }

    private val dataStore = context.dataStore

    private val _isDataLoaded = MutableStateFlow(false)
    override val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    // Definiamo i flussi sorgente privati PRIMA
    private val playerNameSourceFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) { emit(emptyPreferences()) } else { throw exception }
        }
        .map { preferences -> preferences[PLAYER_NAME_KEY] ?: DEFAULT_PLAYER_NAME }

    private val selectedBackgroundsSourceFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) { emit(emptyPreferences()) } else { throw exception }
        }
        .map { preferences ->
            val currentSelection = preferences[SELECTED_BACKGROUNDS_KEY]
            if (currentSelection.isNullOrEmpty()) {
                Log.d("DataStore_Map", "SelectedBackgrounds: No value in Prefs or empty, returning DEFAULT_SELECTED_BACKGROUNDS")
                DEFAULT_SELECTED_BACKGROUNDS 
            } else { 
                Log.d("DataStore_Map", "SelectedBackgrounds: Found value in Prefs: $currentSelection")
                currentSelection 
            }
        }

    private val selectedCardsSourceFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) { emit(emptyPreferences()) } else { throw exception }
        }
        .map { preferences ->
            val currentSelection = preferences[SELECTED_CARDS_KEY]
            if (currentSelection.isNullOrEmpty()) {
                Log.w("DataStore_Map", "SelectedCards: No value in Prefs or empty for key '${SELECTED_CARDS_KEY.name}', RETURNING DEFAULT_SELECTED_CARDS")
                DEFAULT_SELECTED_CARDS 
            } else { 
                Log.d("DataStore_Map", "SelectedCards: Found value in Prefs for key '${SELECTED_CARDS_KEY.name}': $currentSelection")
                currentSelection 
            }
        }

    // ORA definiamo gli StateFlow pubblici (condivisi)
    override val playerName: StateFlow<String> = playerNameSourceFlow.stateIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DEFAULT_PLAYER_NAME
    )

    override val selectedBackgrounds: StateFlow<Set<String>> = selectedBackgroundsSourceFlow.stateIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DEFAULT_SELECTED_BACKGROUNDS
    )

    override val selectedCards: StateFlow<Set<String>> = selectedCardsSourceFlow.stateIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DEFAULT_SELECTED_CARDS // Qui NON usiamo la sentinella per ora, testiamo prima questo approccio
    )

    // E POI il blocco init che opera sugli StateFlow pubblici
    init {
        externalScope.launch {
            Log.d("DataStore", "Initializing RealAppSettingsDataStore - waiting for shared StateFlows to emit their first value...")
            
            // Attendiamo che ciascuno degli StateFlow PUBBLICI emetta il suo primo valore.
            val pName = playerName.first() 
            Log.d("DataStore", "Shared playerName StateFlow has emitted: $pName")
            
            val sBg = selectedBackgrounds.first()
            Log.d("DataStore", "Shared selectedBackgrounds StateFlow has emitted: $sBg")
            
            val sCards = selectedCards.first() // Questo leggerà dallo StateFlow condiviso this.selectedCards
            Log.d("DataStore", "Shared selectedCards StateFlow has emitted: $sCards")
            
            Log.d("DataStore", "All shared StateFlows have emitted. Setting isDataLoaded to true.")
            _isDataLoaded.value = true
        }
    }

    override suspend fun savePlayerName(name: String) {
        dataStore.edit { settings -> settings[PLAYER_NAME_KEY] = name }
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        dataStore.edit { settings -> settings[SELECTED_BACKGROUNDS_KEY] = backgrounds }
    }

    override suspend fun saveSelectedCards(selectedCardsToSave: Set<String>) {
        Log.d("DataStore", "saveSelectedCards - About to edit DataStore for key '${SELECTED_CARDS_KEY.name}' with: $selectedCardsToSave")
        dataStore.edit { preferences ->
            preferences[SELECTED_CARDS_KEY] = selectedCardsToSave
            Log.d("DataStore", "saveSelectedCards - DataStore edit block completed for: $selectedCardsToSave")
        }
        Log.d("DataStore", "saveSelectedCards - Exiting function after edit for: $selectedCardsToSave")
    }
}