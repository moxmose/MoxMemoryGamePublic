package com.example.moxmemorygame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey // Importato per Set<String>
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

// Create instance of DataStore at the file level (singleton for the app)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RealAppSettingsDataStore(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : IAppSettingsDataStore {

    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val CARD_SET_KEY = stringPreferencesKey("card_set")
        // Modificata la chiave per gli sfondi selezionati
        val SELECTED_BACKGROUNDS_KEY = stringSetPreferencesKey("selected_backgrounds")

        // Constants for default values
        const val DEFAULT_PLAYER_NAME = "Default Player"
        const val DEFAULT_CARD_SET = "default_set"
        // Modificato il valore di default per gli sfondi (ora un Set, il primo sfondo come default)
        val DEFAULT_SELECTED_BACKGROUNDS = setOf("background_00")
    }

    private val dataStore = context.dataStore

    override val playerName: StateFlow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PLAYER_NAME_KEY] ?: DEFAULT_PLAYER_NAME
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_PLAYER_NAME
        )

    override suspend fun savePlayerName(name: String) {
        dataStore.edit { settings ->
            settings[PLAYER_NAME_KEY] = name
        }
    }

    override val cardSet: StateFlow<String> = dataStore.data // Potrebbe necessitare di modifiche per selezione multipla
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CARD_SET_KEY] ?: DEFAULT_CARD_SET
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_CARD_SET
        )

    override suspend fun saveCardSet(newSet: String) { // Potrebbe necessitare di modifiche per selezione multipla
        dataStore.edit { settings ->
            settings[CARD_SET_KEY] = newSet
        }
    }

    // Flusso per leggere gli sfondi selezionati
    override val selectedBackgrounds: StateFlow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Legge il Set<String>, se non presente o vuoto, usa il default
            val currentSelection = preferences[SELECTED_BACKGROUNDS_KEY]
            if (currentSelection.isNullOrEmpty()) {
                DEFAULT_SELECTED_BACKGROUNDS
            } else {
                currentSelection
            }
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_SELECTED_BACKGROUNDS
        )

    // Funzione per salvare gli sfondi selezionati
    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        dataStore.edit { settings ->
            settings[SELECTED_BACKGROUNDS_KEY] = backgrounds
        }
    }
}