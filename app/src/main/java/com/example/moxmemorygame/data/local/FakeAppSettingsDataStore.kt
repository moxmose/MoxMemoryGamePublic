package com.example.moxmemorygame.data.local // Pacchetto aggiornato

import android.util.Log
import com.example.moxmemorygame.model.ScoreEntry
// Importa IAppSettingsDataStore dalla sua nuova posizione
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
// Importa RealAppSettingsDataStore per accedere a DEFAULT_SELECTED_CARDS
import com.example.moxmemorygame.data.local.RealAppSettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Updated FakeAppSettingsDataStore to include ranking and last played entry
// La classe è stata spostata qui da ui/PreferencesScreen.kt
class FakeAppSettingsDataStore : IAppSettingsDataStore { // Nome classe semplificato
    private val _playerName = MutableStateFlow("Test Player")
    override val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _selectedBackgrounds = MutableStateFlow(setOf("background_00", "background_01"))
    override val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    // Utilizza il default da RealAppSettingsDataStore per coerenza
    private val _selectedCards = MutableStateFlow(RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
    override val selectedCards: StateFlow<Set<String>> = _selectedCards.asStateFlow()

    private val _topRanking = MutableStateFlow<List<ScoreEntry>>(emptyList())
    override val topRanking: StateFlow<List<ScoreEntry>> = _topRanking.asStateFlow()

    private val _lastPlayedEntry = MutableStateFlow<ScoreEntry?>(null)
    override val lastPlayedEntry: StateFlow<ScoreEntry?> = _lastPlayedEntry.asStateFlow()

    override val isDataLoaded: StateFlow<Boolean> = MutableStateFlow(true) // Per i fake, assumiamo sia sempre caricato

    override suspend fun savePlayerName(name: String) {
        _playerName.value = name
        Log.d("FakeDataStore", "Saved player name: $name")
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        _selectedBackgrounds.value = backgrounds
        Log.d("FakeDataStore", "Saved backgrounds: $backgrounds")
    }

    override suspend fun saveSelectedCards(selectedCardsToSave: Set<String>) {
        _selectedCards.value = selectedCardsToSave
        Log.d("FakeDataStore", "Saved cards: $selectedCardsToSave")
    }

    override suspend fun saveScore(playerName: String, score: Int) {
        val newEntry = ScoreEntry(playerName, score, System.currentTimeMillis())
        _lastPlayedEntry.value = newEntry
        
        val currentRanking = _topRanking.value.toMutableList()
        currentRanking.add(newEntry)
        _topRanking.value = currentRanking
            .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
            .take(ScoreEntry.MAX_RANKING_ENTRIES)
        
        Log.d("FakeDataStore", "Saved score: $newEntry. New ranking: ${_topRanking.value}")
    }
}