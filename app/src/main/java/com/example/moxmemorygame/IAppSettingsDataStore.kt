package com.example.moxmemorygame

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for accessing and storing application settings.
 * This typically involves user preferences like player name, selected game assets, etc.
 */
interface IAppSettingsDataStore {
    /**
     * The current player's name as a [StateFlow].
     */
    val playerName: StateFlow<String>

    /**
     * A [StateFlow] emitting the set of currently selected background resource names.
     * E.g., setOf("background_00", "background_01").
     */
    val selectedBackgrounds: StateFlow<Set<String>> // Modificato da backgroundPreference: StateFlow<String>

    /**
     * A [StateFlow] emitting the set of currently selected card resource names.
     * These are the actual drawable names, e.g., setOf("img_c_00", "img_s_05").
     */
    val selectedCards: StateFlow<Set<String>>

    /**
     * A [StateFlow] indicating whether the DataStore has finished its initial load.
     * `true` if the initial data has been read from disk, `false` otherwise.
     * This can be used by consumers to wait for the data to be ready on cold starts.
     */
    val isDataLoaded: StateFlow<Boolean>

    /**
     * Saves the player's name.
     * @param name The new name to save.
     */
    suspend fun savePlayerName(name: String)

    /**
     * Saves the set of selected background resource names.
     * @param backgrounds A set of strings, where each string is a background resource name.
     */
    suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) // Modificato da saveBackgroundPreference(preference: String)

    /**
     * Saves the set of selected card resource names.
     * @param selectedCards A set of strings, where each string is a card resource name (e.g., "img_c_00").
     */
    suspend fun saveSelectedCards(selectedCards: Set<String>)
}
