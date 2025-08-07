package com.example.moxmemorygame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

//import androidx.paging.map

// Create instance of DataStore at the file level (singleton for the app)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettingsDataStore(private val context: Context) {

    // Define keys for your preferences
    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val CARD_SET_KEY =
            stringPreferencesKey("card_set") // Potresti usare intPreferencesKey se hai ID numerici
        val BACKGROUND_PREFERENCE_KEY =
            stringPreferencesKey("background_preference") // Es: "random", "fixed_id_1"
        // Aggiungi altre chiavi se necessario
    }

    // Flux to read the player name
    val playerNameFlow: Flow<String> = context.dataStore.data
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

    // Function to save Playername
    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { settings ->
            settings[PLAYER_NAME_KEY] = name
        }
    }

    // Flux to read the card set
    val cardSetFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CARD_SET_KEY] ?: "default_set" // Give a default value
        }

    // Function to save the card set
    suspend fun saveCardSet(cardSet: String) {
        context.dataStore.edit { settings ->
            settings[CARD_SET_KEY] = cardSet
        }
    }

    // Flux to read the background preference
    val backgroundPreferenceFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[BACKGROUND_PREFERENCE_KEY] ?: "random" // Give a default value
        }

    // Function to save the background preference
    suspend fun saveBackgroundPreference(preference: String) {
        context.dataStore.edit { settings ->
            settings[BACKGROUND_PREFERENCE_KEY] = preference
        }
    }

    // Others if needed
}