package com.example.moxmemorygame.ui

import android.util.Log // Assicurati che Log sia importato
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.data.local.RealAppSettingsDataStore // Per fallback
import com.example.moxmemorygame.model.GameBoard // Nuovo Import
import com.example.moxmemorygame.model.GameCard // Nuovo Import
import com.example.moxmemorygame.model.BOARD_WIDTH // Nuovo Import
import com.example.moxmemorygame.model.BOARD_HEIGHT // Nuovo Import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first // Import per .first() su Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val navController: NavHostController,
    private val timerViewModel: TimerViewModel,
    private val appSettingsDataStore: IAppSettingsDataStore,
    private val resourceNameToId: (String) -> Int
): ViewModel() {
    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    val selectedBackgrounds: StateFlow<Set<String>> = appSettingsDataStore.selectedBackgrounds

    private val _score = mutableIntStateOf(0)
    val score get() = _score

    private val _moves = mutableIntStateOf(0)
    val moves get() = _moves

    private var _gamePlayResetSound = mutableStateOf(false)
    val gamePlayResetSound get() = _gamePlayResetSound.value

    private var lastMove: Pair<Int, Int> = Pair(0,0)
    private val noLastMove = Pair(-1, -1)
    private var cardInPlay: Boolean = false

    private val _tablePlay = GameBoard() // Modificato da GameCardArray a GameBoard
    val tablePlay: GameBoard get() = _tablePlay // Modificato da GameCardArray a GameBoard

    @DrawableRes
    private lateinit var _gameCardImages: List<Int>
    val gameCardImages get() = _gameCardImages

    val currentTime = timerViewModel.elapsedSeconds
    private var timeOfLastMove = 0L

    var gamePaused: MutableState<Boolean> = mutableStateOf(false)
    var gameResetRequest: MutableState<Boolean> = mutableStateOf(false)
    var gameWon: MutableState<Boolean> = mutableStateOf(false)

    init {
        Log.d("GameVM", "init - Calling resetGame()")
        resetGame()
    }

    private fun resetGame() {
        Log.d("GameVM", "resetGame - Starting game reset process")
        viewModelScope.launch {
            Log.d("GameVM", "resetGame - Coroutine launched, calling loadAndShuffleCards()")
            loadAndShuffleCards()
            
            Log.d("GameVM", "resetGame - loadAndShuffleCards() completed, proceeding with main thread state reset")
            withContext(Dispatchers.Main) {
                _score.intValue = 0
                _moves.intValue = 0
                lastMove = noLastMove
                cardInPlay = false
                _gamePlayResetSound.value = true
                gamePaused.value = false
                gameResetRequest.value = false
                gameWon.value = false
                timerViewModel.resetTimer()
                timerViewModel.startTimer()
                timeOfLastMove = 0L
                Log.d("GameVM", "resetGame - Main thread state reset completed")
            }
        }
    }

    private suspend fun loadAndShuffleCards() {
        Log.d("GameVM", "loadAndShuffleCards - Waiting for DataStore to be loaded...")
        appSettingsDataStore.isDataLoaded.filter { it }.first() 
        Log.d("GameVM", "loadAndShuffleCards - DataStore is loaded. Proceeding to load cards.")

        val uniqueCardsNeeded = (BOARD_WIDTH * BOARD_HEIGHT) / 2
        var userSelectedResourceNames = appSettingsDataStore.selectedCards.first()
        Log.d("GameVM", "loadAndShuffleCards - Read from DataStore: $userSelectedResourceNames")

        if (userSelectedResourceNames.size < uniqueCardsNeeded) {
            Log.w("GameVM", "loadAndShuffleCards - User selected cards insufficient (${userSelectedResourceNames.size} < $uniqueCardsNeeded). Falling back to default.")
            userSelectedResourceNames = RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS
        }
        
        val actualCardResourceNamesForGame = userSelectedResourceNames.shuffled().take(uniqueCardsNeeded)
        Log.d("GameVM", "loadAndShuffleCards - Actual card resource names for game: $actualCardResourceNamesForGame")

        _gameCardImages = actualCardResourceNamesForGame.map { resourceName -> resourceNameToId(resourceName) }
        Log.d("GameVM", "loadAndShuffleCards - Converted to resource IDs (_gameCardImages): $_gameCardImages")

        val logicalCardIds = (0 until uniqueCardsNeeded).toList()
        val gameCardLogicalIdsForBoard = (logicalCardIds + logicalCardIds).shuffled()
        Log.d("GameVM", "loadAndShuffleCards - Prepared logical IDs for board: $gameCardLogicalIdsForBoard")

        withContext(Dispatchers.Main) {
            var i = 0
            for(x in (0 until BOARD_WIDTH)) {
                for(y in (0 until BOARD_HEIGHT)) {
                     if (i < gameCardLogicalIdsForBoard.size) { 
                        _tablePlay.cardsArray[x][y].value = GameCard(
                            id = gameCardLogicalIdsForBoard[i++],
                            turned = false,
                            coupled = false
                        )
                    }
                }
            }
            Log.d("GameVM", "loadAndShuffleCards - Finished populating _tablePlay on Main thread")
        }
    }

    fun onResetAndGoToOpeningMenu() {
        navController.navigate(Screen.OpeningMenuScreen.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun checkGamePlayCardTurned(x: Int, y: Int,
                                flipSound: () -> Unit,
                                pauseSound: () -> Unit, 
                                failSound: () -> Unit,
                                successSound: () -> Unit,
                                winSound: () -> Unit
    ) {
        if (cardInPlay) { return }
        cardInPlay = true

        if (_tablePlay.cardsArray[x][y].value.coupled) {
            cardInPlay = false
            return
        }

        if (lastMove == noLastMove && !_tablePlay.cardsArray[x][y].value.turned) {
            lastMove = Pair(x, y)
            flipSound()
            _moves.intValue++
            setTablePlayCardTurned(x = x,y = y, newTurnedState = true)
            cardInPlay = false
            return
        } else {
            _moves.intValue++
            if (lastMove == Pair(x, y)) {
                refreshPointsNoCoupleSelected()
                lastMove = noLastMove
                flipSound()
                setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                cardInPlay = false
                return
            } else {
                setTablePlayCardTurned(x = x,y = y, newTurnedState = true)
                if (_tablePlay.cardsArray[lastMove.first][lastMove.second].value.id ==
                    _tablePlay.cardsArray[x][y].value.id
                ) {
                    refreshPointsRightCouple()
                    // Ora che GameCard è una data class, usiamo copy() per modificare lo stato coupled
                    _tablePlay.cardsArray[lastMove.first][lastMove.second].value = _tablePlay.cardsArray[lastMove.first][lastMove.second].value.copy(coupled = true)
                    _tablePlay.cardsArray[x][y].value = _tablePlay.cardsArray[x][y].value.copy(coupled = true)
                    
                    if(checkAllCardsCoupled()) {
                        winSound()
                        gameWon.value = true
                        setResetPause()
                        // Salva il punteggio quando il gioco è vinto
                        viewModelScope.launch {
                            val pName = appSettingsDataStore.playerName.first()
                            val finalScore = _score.intValue
                            appSettingsDataStore.saveScore(pName, finalScore)
                            Log.d("GameVM", "Game won! Saved score: $finalScore for player: $pName")
                        }
                    } else {
                        successSound()
                    }
                    lastMove = noLastMove
                    cardInPlay = false
                    return
                } else {
                    refreshPointsWrongCouple()
                    failSound()
                    viewModelScope.launch(Dispatchers.Default) {
                        delay(1400L)
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
        require(x in _tablePlay.cardsArray.indices) { "x coordinate is out of bounds" }
        require(y in _tablePlay.cardsArray[x].indices) { "y coordinate is out of bounds" }
        // Utilizza il metodo copy() fornito dalla data class GameCard
        with(_tablePlay.cardsArray[x][y]) { value = value.copy(turned = newTurnedState) }
    }

    fun setResetPause() {
        if (!gamePaused.value) {
            gamePaused.value = true
            timerViewModel.pauseTimer()
        } else {
            gamePaused.value = false
            timerViewModel.resumeTimer()
        }
    }

    fun setResetReset() {
        if(!gameResetRequest.value) {
            gameResetRequest.value = true
            setResetPause()
        } else {
            gameResetRequest.value = false
            setResetPause()
        }
    }

    fun resetProceed() {
        resetGame()
    }

    fun resetPlayResetSound(resetSound: () -> Unit) {
        resetSound()
        _gamePlayResetSound.value = false
    }

    private fun checkAllCardsCoupled(): Boolean {
        return tablePlay.cardsArray.flatten().all { it.value.coupled }
    }

    private fun getPointsResetTimeLastMove(multiplier: Float): Int {
        val timerPoints = ((currentTime.value - timeOfLastMove) * multiplier).toInt()
        timeOfLastMove = currentTime.value
        return timerPoints
    }

    private fun refreshPointsWrongCouple() {_score.intValue -= (3 + getPointsResetTimeLastMove(0.4f))}
    private fun refreshPointsRightCouple() {_score.intValue += (12 - getPointsResetTimeLastMove(0.2f))}
    private fun refreshPointsNoCoupleSelected() {_score.intValue -= (1 + getPointsResetTimeLastMove(0.5f))}

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            timerViewModel.stopAndAwaitTimerCompletion()
        }
    }
}
