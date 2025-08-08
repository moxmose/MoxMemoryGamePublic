package com.example.moxmemorygame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

//import androidx.paging.map

// Create instance of DataStore at the file level (singleton for the app)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettingsDataStore(
    private val context: Context,
    // Other than testing pourposes, the Scope may survive for Scopes that survives a specific ViewModel
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    // Define keys for your preferences
    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val CARD_SET_KEY =
            stringPreferencesKey("card_set") // Potresti usare intPreferencesKey se hai ID numerici
        val BACKGROUND_PREFERENCE_KEY =
            stringPreferencesKey("background_preference") // Es: "random", "fixed_id_1"
        // Aggiungi altre chiavi se necessario

        // Other keys if needed

        // Constants for default values
        const val DEFAULT_PLAYER_NAME = "Default Player"
        const val DEFAULT_CARD_SET = "default_set"
        const val DEFAULT_BACKGROUND_PREFERENCE = "random"
    }

    private val dataStore = context.dataStore

    // Flux to read the player name
    val playerName: StateFlow<String> = dataStore.data
        .catch { exception ->
            // IOException is expected if the prefs file is tot created yet
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PLAYER_NAME_KEY] ?: "Default Player" // Give a default value
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_PLAYER_NAME
        )

    // Function to save Playername
    suspend fun savePlayerName(name: String) {
        dataStore.edit { settings ->
            settings[PLAYER_NAME_KEY] = name
        }
    }

    // Flux to read the card set
    val cardSet: StateFlow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CARD_SET_KEY] ?: DEFAULT_CARD_SET // Give a default value
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_CARD_SET
        )

    // Function to save the card set
    suspend fun saveCardSet(cardSet: String) {
        dataStore.edit { settings ->
            settings[CARD_SET_KEY] = cardSet
        }
    }

    // Flux to read the background preference
    val backgroundPreference: StateFlow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[BACKGROUND_PREFERENCE_KEY] ?: DEFAULT_BACKGROUND_PREFERENCE // Give a default value
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_BACKGROUND_PREFERENCE
        )

    // Function to save the background preference
    suspend fun saveBackgroundPreference(preference: String) {
        dataStore.edit { settings ->
            settings[BACKGROUND_PREFERENCE_KEY] = preference
        }
    }

    // Others if needed
}