package com.example.moxmemorygame.data.local

import com.example.moxmemorygame.model.BackgroundMusic
import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.flow.StateFlow

interface IAppSettingsDataStore {

    companion object {
        const val DEFAULT_PLAYER_NAME = "Player"
        val DEFAULT_SELECTED_BACKGROUNDS = setOf("background_00")
        val DEFAULT_SELECTED_CARDS = setOf(
            "img_c_00", "img_c_01", "img_c_02", "img_c_03", "img_c_04",
            "img_c_05", "img_c_06", "img_c_07", "img_c_08", "img_c_09",
            "img_c_10", "img_c_11", "img_c_12", "img_c_13", "img_c_14",
            "img_c_15", "img_c_16", "img_c_17", "img_c_18", "img_c_19"
        )
        const val DEFAULT_BOARD_WIDTH = 3
        const val DEFAULT_BOARD_HEIGHT = 4
        val DEFAULT_MUSIC_TRACKS: Set<String> = BackgroundMusic.allTrackNames
        const val DEFAULT_IS_MUSIC_ENABLED = true
        const val DEFAULT_MUSIC_VOLUME = 0.3f
        const val DEFAULT_ARE_SOUND_EFFECTS_ENABLED = true
        const val DEFAULT_SOUND_EFFECTS_VOLUME = 0.7f
    }

    val playerName: StateFlow<String>
    val selectedBackgrounds: StateFlow<Set<String>>
    val selectedCards: StateFlow<Set<String>>
    val topRanking: StateFlow<List<ScoreEntry>>
    val lastPlayedEntry: StateFlow<ScoreEntry?>
    val isDataLoaded: StateFlow<Boolean>
    val selectedBoardWidth: StateFlow<Int>
    val selectedBoardHeight: StateFlow<Int>
    val isFirstTimeLaunch: StateFlow<Boolean>
    val selectedMusicTrackNames: StateFlow<Set<String>>
    val isMusicEnabled: StateFlow<Boolean>
    val musicVolume: StateFlow<Float>
    val areSoundEffectsEnabled: StateFlow<Boolean>
    val soundEffectsVolume: StateFlow<Float>

    suspend fun savePlayerName(name: String)
    suspend fun saveSelectedBackgrounds(backgrounds: Set<String>)
    suspend fun saveSelectedCards(cards: Set<String>)
    suspend fun saveScore(playerName: String, score: Int)
    suspend fun saveBoardDimensions(width: Int, height: Int)
    suspend fun saveIsFirstTimeLaunch(isFirstTime: Boolean)
    suspend fun saveSelectedMusicTracks(trackNames: Set<String>)
    suspend fun saveIsMusicEnabled(isEnabled: Boolean)
    suspend fun saveMusicVolume(volume: Float)
    suspend fun saveAreSoundEffectsEnabled(isEnabled: Boolean)
    suspend fun saveSoundEffectsVolume(volume: Float)
}