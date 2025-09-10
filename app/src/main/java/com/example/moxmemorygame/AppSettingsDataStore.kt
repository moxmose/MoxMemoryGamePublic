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
        // Rimosso CARD_SET_KEY
        // Modificata la chiave per gli sfondi selezionati
        val SELECTED_BACKGROUNDS_KEY = stringSetPreferencesKey("selected_backgrounds")
        // Nuova chiave per le carte selezionate
        val SELECTED_CARDS_KEY = stringSetPreferencesKey("selected_cards")

        // Constants for default values
        const val DEFAULT_PLAYER_NAME = "Default Player"
        // Rimosso DEFAULT_CARD_SET
        // Modificato il valore di default per gli sfondi (ora un Set, il primo sfondo come default)
        val DEFAULT_SELECTED_BACKGROUNDS = setOf("background_00")
        // Valore di default per le carte selezionate (tutte le carte "complex" da img_c_00 a img_c_19)
        val DEFAULT_SELECTED_CARDS = setOf(
            "img_c_00", "img_c_01", "img_c_02", "img_c_03", "img_c_04",
            "img_c_05", "img_c_06", "img_c_07", "img_c_08", "img_c_09",
            "img_c_10", "img_c_11", "img_c_12", "img_c_13", "img_c_14",
            "img_c_15", "img_c_16", "img_c_17", "img_c_18", "img_c_19"
        )
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

    // Rimossa la vecchia implementazione di cardSet e saveCardSet

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

    // Flusso per leggere le carte selezionate
    override val selectedCards: StateFlow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val currentSelection = preferences[SELECTED_CARDS_KEY]
            // Se non ci sono carte salvate o il set è vuoto, usa il default.
            // Questo assicura che ci sia sempre una selezione che rispetti il minimo richiesto.
            if (currentSelection.isNullOrEmpty()) {
                DEFAULT_SELECTED_CARDS // Ora corretto con img_c_00 a img_c_19
            } else {
                currentSelection
            }
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_SELECTED_CARDS // Ora corretto con img_c_00 a img_c_19
        )

    // Funzione per salvare le carte selezionate
    override suspend fun saveSelectedCards(selectedCards: Set<String>) {
        dataStore.edit { settings ->
            settings[SELECTED_CARDS_KEY] = selectedCards
        }
    }
}
