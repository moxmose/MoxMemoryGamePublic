package com.example.moxmemorygame.data.local

import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.delay
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

    private val _selectedMusicTrackNames = MutableStateFlow(IAppSettingsDataStore.DEFAULT_MUSIC_TRACKS)
    override val selectedMusicTrackNames: StateFlow<Set<String>> = _selectedMusicTrackNames.asStateFlow()

    private val _isMusicEnabled = MutableStateFlow(IAppSettingsDataStore.DEFAULT_IS_MUSIC_ENABLED)
    override val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _musicVolume = MutableStateFlow(IAppSettingsDataStore.DEFAULT_MUSIC_VOLUME)
    override val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _areSoundEffectsEnabled = MutableStateFlow(IAppSettingsDataStore.DEFAULT_ARE_SOUND_EFFECTS_ENABLED)
    override val areSoundEffectsEnabled: StateFlow<Boolean> = _areSoundEffectsEnabled.asStateFlow()

    private val _soundEffectsVolume = MutableStateFlow(IAppSettingsDataStore.DEFAULT_SOUND_EFFECTS_VOLUME)
    override val soundEffectsVolume: StateFlow<Float> = _soundEffectsVolume.asStateFlow()

    override val isDataLoaded: StateFlow<Boolean> = MutableStateFlow(true)

    private var saveDelayMillis = 0L

    fun setSaveDelay(delay: Long) {
        saveDelayMillis = delay
    }

    override suspend fun savePlayerName(name: String) {
        delay(saveDelayMillis)
        _playerName.value = name
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        delay(saveDelayMillis)
        _selectedBackgrounds.value = backgrounds
    }

    override suspend fun saveSelectedCards(cards: Set<String>) {
        delay(saveDelayMillis)
        _selectedCards.value = cards
    }

    override suspend fun saveScore(playerName: String, score: Int) {
        delay(saveDelayMillis)
        val newEntry = ScoreEntry(playerName, score, System.currentTimeMillis())
        _lastPlayedEntry.value = newEntry
        
        val currentRanking = _topRanking.value.toMutableList()
        currentRanking.add(newEntry)
        _topRanking.value = currentRanking
            .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.timestamp })
            .take(ScoreEntry.MAX_RANKING_ENTRIES)
    }

    override suspend fun saveBoardDimensions(width: Int, height: Int) {
        delay(saveDelayMillis)
        _selectedBoardWidth.value = width
        _selectedBoardHeight.value = height
    }

    override suspend fun saveIsFirstTimeLaunch(isFirstTime: Boolean) {
        delay(saveDelayMillis)
        _isFirstTimeLaunch.value = isFirstTime
    }

    override suspend fun saveSelectedMusicTracks(trackNames: Set<String>) {
        delay(saveDelayMillis)
        _selectedMusicTrackNames.value = trackNames
    }

    override suspend fun saveIsMusicEnabled(isEnabled: Boolean) {
        delay(saveDelayMillis)
        _isMusicEnabled.value = isEnabled
    }

    override suspend fun saveMusicVolume(volume: Float) {
        delay(saveDelayMillis)
        _musicVolume.value = volume
    }

    override suspend fun saveAreSoundEffectsEnabled(isEnabled: Boolean) {
        delay(saveDelayMillis)
        _areSoundEffectsEnabled.value = isEnabled
    }

    override suspend fun saveSoundEffectsVolume(volume: Float) {
        delay(saveDelayMillis)
        _soundEffectsVolume.value = volume
    }
}