package com.example.moxmemorygame.ui

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.GameBoard
import com.example.moxmemorygame.model.GameCard
import com.example.moxmemorygame.model.SoundEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

class GameViewModel(
    private val navController: NavHostController,
    private val timerViewModel: TimerViewModel,
    private val appSettingsDataStore: IAppSettingsDataStore,
    private val resourceNameToId: (String) -> Int,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val delayProvider: suspend (Long) -> Unit = { delay(it) }
): ViewModel() {
    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    val selectedBackgrounds: StateFlow<Set<String>> = appSettingsDataStore.selectedBackgrounds

    private val _score = mutableIntStateOf(0)
    val score get() = _score

    private val _moves = mutableIntStateOf(0)
    val moves get() = _moves

    private var lastMove: Pair<Int, Int> = Pair(0,0)
    private val noLastMove = Pair(-1, -1)
    private var cardInPlay: Boolean = false

    private var _tablePlay: GameBoard? = null
    val tablePlay: GameBoard? get() = _tablePlay

    @DrawableRes
    private lateinit var _gameCardImages: List<Int>
    val gameCardImages get() = _gameCardImages

    val currentTime = timerViewModel.elapsedSeconds
    private var timeOfLastMove = 0L

    var gamePaused: MutableState<Boolean> = mutableStateOf(false)
    var gameResetRequest: MutableState<Boolean> = mutableStateOf(false)
    var gameWon: MutableState<Boolean> = mutableStateOf(false)

    private val _isBoardInitialized = mutableStateOf(false)
    val isBoardInitialized: State<Boolean> = _isBoardInitialized

    private val _playResetSound = MutableStateFlow(false)
    val playResetSound: StateFlow<Boolean> = _playResetSound.asStateFlow()

    init {
        Log.d("GameVM", "init - Calling resetGame()")
        resetGame()
    }

    private fun resetGame() {
        Log.d("GameVM", "resetGame - Starting game reset process")
        viewModelScope.launch {
            _isBoardInitialized.value = false 
            _tablePlay = null 
            Log.d("GameVM", "resetGame - _isBoardInitialized set to false, _tablePlay set to null. Calling loadAndShuffleCards()")
            loadAndShuffleCards()
            
            Log.d("GameVM", "resetGame - loadAndShuffleCards() completed, proceeding with main thread state reset for UI elements")
            withContext(Dispatchers.Main) {
                _score.intValue = 0 
                _moves.intValue = 0
                lastMove = noLastMove
                cardInPlay = false
                _playResetSound.value = true // Signal the UI to play the sound
                // These states are crucial for dialogs, make sure they are clean after a full reset.
                gamePaused.value = false
                gameResetRequest.value = false
                gameWon.value = false 
                timerViewModel.resetTimer()
                timerViewModel.startTimer()
                timeOfLastMove = 0L
                Log.d("GameVM", "resetGame - Main thread UI state reset completed")
            }
        }
    }

    fun onResetSoundPlayed() {
        _playResetSound.value = false
    }

    private suspend fun loadAndShuffleCards() {
        Log.d("GameVM", "loadAndShuffleCards - Waiting for DataStore to be loaded...")
        appSettingsDataStore.isDataLoaded.filter { it }.first() 
        Log.d("GameVM", "loadAndShuffleCards - DataStore is loaded. Proceeding to load cards.")

        val boardWidth = appSettingsDataStore.selectedBoardWidth.first()
        val boardHeight = appSettingsDataStore.selectedBoardHeight.first()
        Log.d("GameVM", "loadAndShuffleCards - Fetched board dimensions: ${boardWidth}x${boardHeight}")

        _tablePlay = GameBoard(boardWidth, boardHeight)
        Log.d("GameVM", "loadAndShuffleCards - _tablePlay instance created with new dimensions.")

        val uniqueCardsNeeded = (boardWidth * boardHeight) / 2
        var userSelectedResourceNames = appSettingsDataStore.selectedCards.first()

        if (userSelectedResourceNames.size < uniqueCardsNeeded) {
            Log.w("GameVM", "loadAndShuffleCards - User selected cards insufficient. Falling back to default.")
            userSelectedResourceNames = IAppSettingsDataStore.DEFAULT_SELECTED_CARDS
        }
        
        val actualCardResourceNamesForGame = userSelectedResourceNames.shuffled().take(uniqueCardsNeeded)
        _gameCardImages = actualCardResourceNamesForGame.map { resourceName -> resourceNameToId(resourceName) }
        Log.d("GameVM", "loadAndShuffleCards - Card images prepared.")

        val logicalCardIds = (0 until uniqueCardsNeeded).toList()
        val gameCardLogicalIdsForBoard = (logicalCardIds + logicalCardIds).shuffled()

        withContext(Dispatchers.Main) {
            val board = _tablePlay
            if (board == null) {
                Log.e("GameVM", "loadAndShuffleCards - CRITICAL: _tablePlay is null before populating on Main thread. Aborting population.")
                _isBoardInitialized.value = false 
                return@withContext
            }

            var i = 0
            for(x in (0 until boardWidth)) {
                for(y in (0 until boardHeight)) {
                     if (i < gameCardLogicalIdsForBoard.size) { 
                        board.cardsArray[x][y].value = GameCard(
                            id = gameCardLogicalIdsForBoard[i++],
                            turned = false,
                            coupled = false
                        )
                    } else {
                        Log.e("GameVM", "loadAndShuffleCards - Error: Not enough logical card IDs for board size!") 
                    }
                }
            }
            Log.d("GameVM", "loadAndShuffleCards - Finished populating _tablePlay on Main thread.")
            _isBoardInitialized.value = true 
            Log.d("GameVM", "loadAndShuffleCards - _isBoardInitialized set to true.")
        }
    }

    // Called from GameWonDialog and ResetDialog confirmation to go back to the menu
    fun navigateToOpeningMenuAndCleanupDialogStates() {
        Log.d("GameVM", "navigateToOpeningMenuAndCleanupDialogStates - Cleaning dialog states and navigating.")
        gamePaused.value = false
        gameResetRequest.value = false
        gameWon.value = false // Ensures the win state is reset
        navController.navigate(Screen.OpeningMenuScreen.route) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun checkGamePlayCardTurned(x: Int, y: Int, onSoundEvent: (SoundEvent) -> Unit) {
        if (!isBoardInitialized.value) { 
            Log.w("GameVM_Check", "Board not initialized. Ignoring.")
            return
        }
        val currentBoard = _tablePlay
        if (currentBoard == null) {
            Log.e("GameVM_Check", "CRITICAL: _tablePlay is NULL. Aborting.")
            cardInPlay = false
            return
        }
        if (cardInPlay) { return }
        cardInPlay = true

        val cardStateCurrentInitial = currentBoard.cardsArray.getOrNull(x)?.getOrNull(y)
        if (cardStateCurrentInitial == null) {
            Log.e("GameVM_Check", "CRITICAL: cardStateCurrentInitial at [$x][$y] is null. Aborting turn.")
            cardInPlay = false
            return
        }
        val cardValueCurrentInitial = cardStateCurrentInitial.value
        if (cardValueCurrentInitial == null) {
            Log.e("GameVM_Check", "CRITICAL: cardValueCurrentInitial at [$x][$y] is null. Aborting turn.")
            cardInPlay = false
            return
        }

        if (cardValueCurrentInitial.coupled) {
            cardInPlay = false
            return
        }

        val currentElapsedTime = currentTime.value
        if (lastMove == noLastMove && !cardValueCurrentInitial.turned) {
            timeOfLastMove = currentElapsedTime
            lastMove = Pair(x, y)
            onSoundEvent(SoundEvent.Flip)
            _moves.intValue++
            setTablePlayCardTurned(x = x,y = y, newTurnedState = true)
            cardInPlay = false
            return
        } else {
            _moves.intValue++
            if (lastMove == Pair(x, y)) {
                refreshPointsNoCoupleSelected(currentElapsedTime - timeOfLastMove)
                lastMove = noLastMove
                onSoundEvent(SoundEvent.Flip)
                setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                cardInPlay = false
                return
            } else {
                setTablePlayCardTurned(x = x,y = y, newTurnedState = true)
                
                val cardStateLast = currentBoard.cardsArray.getOrNull(lastMove.first)?.getOrNull(lastMove.second)
                if (cardStateLast == null) {
                    Log.e("GameVM_Check", "CRITICAL: cardStateLast at [${lastMove.first}][${lastMove.second}] is null. Aborting comparison.")
                    lastMove = noLastMove
                    setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                    cardInPlay = false
                    return
                }
                val cardValueLast = cardStateLast.value
                if (cardValueLast == null) {
                    Log.e("GameVM_Check", "CRITICAL: cardValueLast at [${lastMove.first}][${lastMove.second}] is null. Aborting comparison.")
                    lastMove = noLastMove
                    setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                    cardInPlay = false
                    return
                }

                val cardStateCurrentAfterTurn = currentBoard.cardsArray.getOrNull(x)?.getOrNull(y)
                if (cardStateCurrentAfterTurn == null) {
                    Log.e("GameVM_Check", "CRITICAL: cardStateCurrentAfterTurn at [$x][$y] is null. Aborting comparison.")
                    lastMove = noLastMove 
                    cardInPlay = false
                    return
                }
                val cardValueCurrentAfterTurn = cardStateCurrentAfterTurn.value
                if (cardValueCurrentAfterTurn == null) {
                    Log.e("GameVM_Check", "CRITICAL: cardValueCurrentAfterTurn at [$x][$y] is null. Aborting comparison.")
                    lastMove = noLastMove 
                    cardInPlay = false
                    return
                }

                if (cardValueLast.id == cardValueCurrentAfterTurn.id) {
                    refreshPointsRightCouple(currentElapsedTime - timeOfLastMove)
                    cardStateLast.value = cardValueLast.copy(coupled = true)
                    cardStateCurrentAfterTurn.value = cardValueCurrentAfterTurn.copy(coupled = true)
                    timeOfLastMove = currentElapsedTime
                    
                    if(checkAllCardsCoupled()) {
                        onSoundEvent(SoundEvent.Win)
                        gameWon.value = true
                        timerViewModel.stopTimer()
                        requestPauseDialog() // Show GameWonDialog via gamePaused=true
                        viewModelScope.launch {
                            val pName = appSettingsDataStore.playerName.first()
                            val finalScore = _score.intValue 
                            appSettingsDataStore.saveScore(pName, finalScore) 
                            Log.d("GameVM", "Game won! Saved score: $finalScore for player: $pName")
                        }
                    } else {
                        onSoundEvent(SoundEvent.Success)
                    }
                    lastMove = noLastMove
                    cardInPlay = false
                    return
                } else {
                    refreshPointsWrongCouple(currentElapsedTime - timeOfLastMove)
                    onSoundEvent(SoundEvent.Fail)
                    timeOfLastMove = currentElapsedTime
                    viewModelScope.launch(ioDispatcher) {
                        delayProvider(1400L)
                        withContext(Dispatchers.Main) {
                            setTablePlayCardTurned(x = lastMove.first,y = lastMove.second, newTurnedState = false)
                            setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                            lastMove = noLastMove
                            cardInPlay = false
                        }
                    }
                }
            }
        }
    }

    private fun setTablePlayCardTurned(x: Int, y: Int, newTurnedState: Boolean) {
        if (!isBoardInitialized.value) return
        val currentBoard = _tablePlay
        if (currentBoard == null) {
            Log.e("GameVM_SetTurned", "CRITICAL: _tablePlay is NULL. Aborting update.")
            return
        }
        
        val cardState = currentBoard.cardsArray.getOrNull(x)?.getOrNull(y)
        if (cardState == null) {
            Log.e("GameVM_SetTurned", "CRITICAL: cardState at [$x][$y] is null. Aborting update.")
            return
        }
        val cardValue = cardState.value
        if (cardValue == null) {
             Log.e("GameVM_SetTurned", "CRITICAL: cardValue at [$x][$y] (from cardState.value) is null. Aborting update.")
            return
        }
        cardState.value = cardValue.copy(turned = newTurnedState)
    }

    // Called by the Pause button or when the game is won
    fun requestPauseDialog() { 
        Log.d("GameVM", "requestPauseDialog - Setting gamePaused = true")
        gamePaused.value = true 
    }

    // Called by the Reset button
    fun requestResetDialog() { 
        Log.d("GameVM", "requestResetDialog - Setting gamePaused = true, gameResetRequest = true")
        gamePaused.value = true
        gameResetRequest.value = true 
    }

    // Called to close the PauseDialog
    fun dismissPauseDialog() {
        Log.d("GameVM", "dismissPauseDialog - Setting gamePaused = false")
        gamePaused.value = false
        // gameResetRequest should already be false, but just in case:
        if (gameResetRequest.value && !gameWon.value) { // Don't reset if it's a confirmed or won reset
            Log.w("GameVM", "dismissPauseDialog - gameResetRequest was true when dismissing pause. Check logic.")
            // gameResetRequest.value = false // Optional: depends on the desired logic
        }
    }

    // Called to cancel the ResetDialog
    fun cancelResetDialog() {
        Log.d("GameVM", "cancelResetDialog - Setting gamePaused = false, gameResetRequest = false")
        gamePaused.value = false
        gameResetRequest.value = false
    }
    
    // Called from the "Reset" button in the game menu to reset the current match
    fun resetCurrentGame() { 
        Log.d("GameVM", "resetCurrentGame - Calling resetGame() to restart current match.")
        resetGame() 
    }

    private fun checkAllCardsCoupled(): Boolean {
        if (!isBoardInitialized.value) {
            Log.w("GameVM_CheckAll", "Board not initialized. Returning false.")
            return false
        }
        val currentBoard = _tablePlay
        if (currentBoard == null) {
            Log.e("GameVM_CheckAll", "CRITICAL: _tablePlay is NULL. Returning false.")
            return false
        }

        for (x_idx in currentBoard.cardsArray.indices) {
            for (y_idx in currentBoard.cardsArray[x_idx].indices) {
                val cardState = currentBoard.cardsArray.getOrNull(x_idx)?.getOrNull(y_idx)
                if (cardState == null) {
                    Log.e("GameVM_CheckAll", "CRITICAL: cardState at [$x_idx][$y_idx] is null during iteration. Game state error.")
                    return false 
                }
                val cardValue = cardState.value
                if (cardValue == null) {
                    Log.e("GameVM_CheckAll", "CRITICAL: cardValue at [$x_idx][$y_idx] is null during iteration. Game state error.")
                    return false 
                }
                if (!cardValue.coupled) {
                    return false 
                }
            }
        }
        return true 
    }

    private fun calculateTimeEffectDeciPoints(timeDeltaInSeconds: Long, effectRateInteger: Int): Int {
        if (timeDeltaInSeconds <= 0) return 0
        val points = (100.0 / timeDeltaInSeconds.toDouble()) * effectRateInteger
        return points.roundToInt()
    }

    private fun calculateBoardDifficultyDeciBonusPoints(): Int {
        if (!isBoardInitialized.value) {
            Log.w("GameVM_Score", "calculateBoardDifficultyDeciBonusPoints - Board not initialized. Returning 0 bonus.")
            return 0
        }
        val currentBoard = _tablePlay
        if (currentBoard == null) {
            Log.e("GameVM_Score", "CRITICAL: calculateBoardDifficultyDeciBonusPoints - _tablePlay is NULL. Returning 0 bonus.")
            return 0
        }

        val minConfigurableBoardCells = PreferencesViewModel.MIN_BOARD_WIDTH * PreferencesViewModel.MIN_BOARD_HEIGHT
        val currentTotalCells = currentBoard.boardWidth * currentBoard.boardHeight

        if (currentTotalCells <= 0) return 0
        if (currentTotalCells <= minConfigurableBoardCells) return 0

        val bonusFloat = 2.0f * (1.0f - (minConfigurableBoardCells.toFloat() / currentTotalCells.toFloat()))
        val deciBonus = (bonusFloat * 10.0f).roundToInt()
        
        return max(0, deciBonus) 
    }

    private fun refreshPointsWrongCouple(timeDeltaInSeconds: Long) { 
        val timePenaltyDeciPoints = calculateTimeEffectDeciPoints(timeDeltaInSeconds, effectRateInteger = -2)
        _score.intValue = _score.intValue + timePenaltyDeciPoints 
        Log.d("GameVM_Score", "refreshPointsWrongCouple - Score: ${_score.intValue}, Time Penalty: $timePenaltyDeciPoints")
    }
    private fun refreshPointsRightCouple(timeDeltaInSeconds: Long) { 
        val timeBonusDeciPoints = calculateTimeEffectDeciPoints(timeDeltaInSeconds, effectRateInteger = 3) // MODIFIED
        val boardBonusDeciPoints = calculateBoardDifficultyDeciBonusPoints()
        _score.intValue = _score.intValue + timeBonusDeciPoints + boardBonusDeciPoints 
        Log.d("GameVM_Score", "refreshPointsRightCouple - Score: ${_score.intValue}, Time Bonus: $timeBonusDeciPoints, Board Bonus: $boardBonusDeciPoints")
    }
    private fun refreshPointsNoCoupleSelected(timeDeltaInSeconds: Long) { 
        val timePenaltyDeciPoints = calculateTimeEffectDeciPoints(timeDeltaInSeconds, effectRateInteger = -1)
        _score.intValue = _score.intValue + timePenaltyDeciPoints 
        Log.d("GameVM_Score", "refreshPointsNoCoupleSelected - Score: ${_score.intValue}, Time Penalty: $timePenaltyDeciPoints")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            timerViewModel.stopAndAwaitTimerCompletion()
        }
    }
}
