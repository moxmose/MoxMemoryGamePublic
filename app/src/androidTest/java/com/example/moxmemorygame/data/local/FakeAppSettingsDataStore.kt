package com.example.moxmemorygame.data.local

import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppSettingsDataStore : IAppSettingsDataStore {
    override val playerName = MutableStateFlow(IAppSettingsDataStore.DEFAULT_PLAYER_NAME)
    override val selectedBackgrounds = MutableStateFlow(IAppSettingsDataStore.DEFAULT_SELECTED_BACKGROUNDS)
    override val selectedCards = MutableStateFlow(IAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
    override val topRanking = MutableStateFlow<List<ScoreEntry>>(emptyList())
    override val lastPlayedEntry = MutableStateFlow<ScoreEntry?>(null)
    override val isDataLoaded = MutableStateFlow(true)
    override val selectedBoardWidth = MutableStateFlow(IAppSettingsDataStore.DEFAULT_BOARD_WIDTH)
    override val selectedBoardHeight = MutableStateFlow(IAppSettingsDataStore.DEFAULT_BOARD_HEIGHT)
    override val isFirstTimeLaunch = MutableStateFlow(false)
    override val selectedMusicTrackNames = MutableStateFlow(IAppSettingsDataStore.DEFAULT_MUSIC_TRACKS)
    override val isMusicEnabled = MutableStateFlow(IAppSettingsDataStore.DEFAULT_IS_MUSIC_ENABLED)
    override val musicVolume = MutableStateFlow(IAppSettingsDataStore.DEFAULT_MUSIC_VOLUME)
    override val areSoundEffectsEnabled = MutableStateFlow(IAppSettingsDataStore.DEFAULT_ARE_SOUND_EFFECTS_ENABLED)
    override val soundEffectsVolume = MutableStateFlow(IAppSettingsDataStore.DEFAULT_SOUND_EFFECTS_VOLUME)

    override suspend fun savePlayerName(name: String) {
        playerName.value = name
    }

    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        selectedBackgrounds.value = backgrounds
    }

    override suspend fun saveSelectedCards(cards: Set<String>) {
        selectedCards.value = cards
    }

    override suspend fun saveScore(playerName: String, score: Int) {
        val newEntry = ScoreEntry(playerName, score, System.currentTimeMillis())
        lastPlayedEntry.value = newEntry
        val updatedRanking = (topRanking.value + newEntry).sortedByDescending { it.score }.take(10)
        topRanking.value = updatedRanking
    }

    override suspend fun saveBoardDimensions(width: Int, height: Int) {
        selectedBoardWidth.value = width
        selectedBoardHeight.value = height
    }

    override suspend fun saveIsFirstTimeLaunch(isFirstTime: Boolean) {
        isFirstTimeLaunch.value = isFirstTime
    }

    override suspend fun saveSelectedMusicTracks(trackNames: Set<String>) {
        selectedMusicTrackNames.value = trackNames
    }

    override suspend fun saveIsMusicEnabled(isEnabled: Boolean) {
        isMusicEnabled.value = isEnabled
    }

    override suspend fun saveMusicVolume(volume: Float) {
        musicVolume.value = volume
    }

    override suspend fun saveAreSoundEffectsEnabled(isEnabled: Boolean) {
        areSoundEffectsEnabled.value = isEnabled
    }

    override suspend fun saveSoundEffectsVolume(volume: Float) {
        soundEffectsVolume.value = volume
    }
}