package com.example.moxmemorygame

import kotlinx.coroutines.flow.StateFlow

interface IAppSettingsDataStore {
    val playerName: StateFlow<String>
    val cardSet: StateFlow<String> // Questo potrebbe aver bisogno di una modifica simile se vuoi selezione multipla anche per i set di carte
    val selectedBackgrounds: StateFlow<Set<String>> // Modificato da backgroundPreference: StateFlow<String>

    suspend fun savePlayerName(name: String)
    suspend fun saveCardSet(newSet: String) // Anche questo, se si modifica cardSet
    suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) // Modificato da saveBackgroundPreference(preference: String)
}