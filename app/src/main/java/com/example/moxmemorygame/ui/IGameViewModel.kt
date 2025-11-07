package com.example.moxmemorygame.ui

import androidx.compose.runtime.State
import com.example.moxmemorygame.model.GameBoard
import com.example.moxmemorygame.model.SoundEvent
import kotlinx.coroutines.flow.StateFlow

interface IGameViewModel {
    val playerName: StateFlow<String> // Added this line
    val tablePlay: GameBoard?
    val isBoardInitialized: State<Boolean>
    val score: State<Int>
    val moves: State<Int>
    val currentTime: StateFlow<Long>
    val gamePaused: State<Boolean>
    val gameResetRequest: State<Boolean>
    val gameWon: State<Boolean>
    val selectedBackgrounds: StateFlow<Set<String>>
    val gameCardImages: List<Int>
    val playResetSound: StateFlow<Boolean>

    fun checkGamePlayCardTurned(x: Int, y: Int, onSoundEvent: (SoundEvent) -> Unit)
    fun navigateToOpeningMenuAndCleanupDialogStates()
    fun requestPauseDialog()
    fun requestResetDialog()
    fun dismissPauseDialog()
    fun cancelResetDialog()
    fun onResetSoundPlayed()
}
