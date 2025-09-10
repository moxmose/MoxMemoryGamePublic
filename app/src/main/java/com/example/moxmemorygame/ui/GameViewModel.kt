package com.example.moxmemorygame.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.IAppSettingsDataStore
import com.example.moxmemorygame.RealAppSettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val navController: NavHostController,
    private val timerViewModel: TimerViewModel,
    private val appSettingsDataStore: IAppSettingsDataStore
): ViewModel() {
    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    val selectedCards: StateFlow<Set<String>> = appSettingsDataStore.selectedCards // Potrebbe necessitare modifiche per selezione multipla
    // Rinominato da backgroundPreference a selectedBackgrounds per coerenza
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

    private val _tablePlay = GameCardArray()
    val tablePlay: GameCardArray get() = _tablePlay

    @DrawableRes
    private lateinit var _gameCardImages: List<Int>
    val gameCardImages get() = _gameCardImages

    val currentTime = timerViewModel.elapsedSeconds
    private var timeOfLastMove = 0L

    var gamePaused: MutableState<Boolean> = mutableStateOf(false)
    var gameResetRequest: MutableState<Boolean> = mutableStateOf(false)
    var gameWon: MutableState<Boolean> = mutableStateOf(false)

    init {
        resetGame()
    }

    private fun resetGame() {
        resetAndShuffleImages()
        _score.intValue = 0
        _moves.intValue = 0
        lastMove = Pair(-1,-1)
        cardInPlay = false
        _gamePlayResetSound.value = true
        gamePaused.value = false
        gameResetRequest.value = false
        gameWon.value = false
        timerViewModel.resetTimer()
        timerViewModel.startTimer()
        timeOfLastMove = 0L
    }

    private fun resetAndShuffleImages() {
        val dim = BOARD_WIDTH * BOARD_HEIGHT /2 -1
        _gameCardImages = GameCardImages().image.shuffled().subList(0,dim+1)

        val gameCardIndexes = ((0..dim) + (0..dim)).shuffled()
        var i = 0
        for(x in (0 until BOARD_WIDTH))
            for(y in (0 until BOARD_HEIGHT))
            _tablePlay.cardsArray[x][y].value = GameCard(
                id = gameCardIndexes[i++],
                turned = false,
                coupled = false
            )
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
                    tablePlay.cardsArray[lastMove.first][lastMove.second].value.coupled = true
                    tablePlay.cardsArray[x][y].value.coupled = true
                    if(checkAllCardsCoupled()) {
                        winSound()
                        gameWon.value = true
                        setResetPause()
                    } else
                        successSound()
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
        with(_tablePlay.cardsArray[x][y]) { value = value.copyChangingTurned(newTurnedState) }
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

    private fun refreshPointsWrongCouple() {_score.value -= (3 + getPointsResetTimeLastMove(0.4f))}
    private fun refreshPointsRightCouple() {_score.value += (12 - getPointsResetTimeLastMove(0.2f))}
    private fun refreshPointsNoCoupleSelected() {_score.value -= (1 + getPointsResetTimeLastMove(0.5f))}

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            timerViewModel.stopAndAwaitTimerCompletion()
        }
    }
}
