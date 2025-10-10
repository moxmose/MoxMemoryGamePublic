package com.example.moxmemorygame.data.local

import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppSettingsDataStore : IAppSettingsDataStore {
    private val _playerName = MutableStateFlow(IAppSettingsDataStore.DEFAULT_PLAYER_NAME)
    override val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _selectedBackgrounds = MutableStateFlow(IAppSettingsDataStore.DEFAULT_SELECTED_BACKGROUNDS)
    override val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    private val _selectedCards = MutableStateFlow(IAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
    override val selectedCards: StateFlow<Set<String>> = _selectedCards.asStateFlow()

    private val _topRanking = MutableStateFlow<List<ScoreEntry>>(emptyList())
    override val topRanking: StateFlow<List<ScoreEntry>> = _topRanking.asStateFlow()

    private val _lastPlayedEntry = MutableStateFlow<ScoreEntry?>(null)
    override val lastPlayedEntry: StateFlow<ScoreEntry?> = _lastPlayedEntry.asStateFlow()

    private val _selectedBoardWidth = MutableStateFlow(IAppSettingsDataStore.DEFAULT_BOARD_WIDTH)
    override val selectedBoardWidth: StateFlow<Int> = _selectedBoardWidth.asStateFlow()

    private val _selectedBoardHeight = MutableStateFlow(IAppSettingsDataStore.DEFAULT_BOARD_HEIGHT)
    override val selectedBoardHeight: StateFlow<Int> = _selectedBoardHeight.asStateFlow()

    private val _isFirstTimeLaunch = MutableStateFlow(true)
    override val isFirstTimeLaunch: StateFlow<Boolean> = _isFirstTimeLaunch.asStateFlow()

    override val isDataLoaded: StateFlow<Boolean> = MutableStateFlow(true)

    override suspend fun savePlayerName(name: String) {
        _playerName.value = name
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        _selectedBackgrounds.value = backgrounds
    }

    override suspend fun saveSelectedCards(cards: Set<String>) {
        _selectedCards.value = cards
    }

    override suspend fun saveScore(playerName: String, score: Int) {
        val newEntry = ScoreEntry(playerName, score, System.currentTimeMillis())
        _lastPlayedEntry.value = newEntry
        
        val currentRanking = _topRanking.value.toMutableList()
        currentRanking.add(newEntry)
        _topRanking.value = currentRanking
            .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
            .take(ScoreEntry.MAX_RANKING_ENTRIES)
    }

    override suspend fun saveBoardDimensions(width: Int, height: Int) {
        _selectedBoardWidth.value = width
        _selectedBoardHeight.value = height
    }

    override suspend fun saveIsFirstTimeLaunch(isFirstTime: Boolean) {
        _isFirstTimeLaunch.value = isFirstTime
    }
}