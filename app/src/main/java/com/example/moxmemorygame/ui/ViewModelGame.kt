package com.example.moxmemorygame.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelGame: ViewModel() {
    private val _score = mutableIntStateOf(0)
    val score get() = _score

    private val _moves = mutableIntStateOf(0)
    val moves get() = _moves

    private var lastMove: Pair<Int, Int> = Pair(0,0)
    private val noLastMove = Pair(-1, -1) // Used to std value
    private var cardInPlay: Boolean = false
    private var gameStarted: Boolean =  false
    private var gameFinished: Boolean = false
    private var gameLost: Boolean = false

    private val _tablePlay = GameCardArray()
    val tablePlay: GameCardArray get() = _tablePlay

    @DrawableRes
    private lateinit var _gameCardImages: List<Int>
    val gameCardImages get() = _gameCardImages

    private val _simpleTimer = MutableStateFlow(0L)
    val simpleTimer = _simpleTimer.asStateFlow()
    private var timeOfLastMove = 0L

    private var simpleTimerJob: Job? = null

    var gamePaused: MutableState<Boolean> = mutableStateOf(false)
    var gameResetRequest: MutableState<Boolean> = mutableStateOf(false)
    var gameWon: MutableState<Boolean> = mutableStateOf(false)

    init {
        resetGame()
    }

    private fun resetGame() {
        resetImages()
        _score.intValue = 0
        _moves.intValue = 0
        lastMove = Pair(-1,-1)
        cardInPlay = false
        gameStarted = false
        gameFinished = false
        gamePaused.value = false
        gameResetRequest.value = false
        gameWon.value = false
        gameLost = false
        stopSimpleTimer()
        startSimpleTimer()
        timeOfLastMove = 0L
    }

    private fun resetImages() {
        val dim = X_BOARD_DIM * Y_BOARD_DIM /2 -1
        _gameCardImages = GameCardImages().image.shuffled().subList(0,dim+1)

        val gameCardIndexes = ((0..dim) + (0..dim)).shuffled()
        var i = 0
        for(x in (0 until X_BOARD_DIM))
            for(y in (0 until Y_BOARD_DIM))
            _tablePlay.cardsArray[x][y].value = GameCard(
                id = gameCardIndexes[i++],
                turned = false,
                coupled = false
            )

    }

    /**
     * Main routine to check how the game is going after card is clicked
     * the logic controls if the card is the second of a couple, yet discovered, wrong
     * and sets the game @cardInPlay to consider one card at time
     */
    fun checkGamePlayCardTurned(x: Int, y: Int,
                                flipSound: () -> Unit,
                                pauseSound: () -> Unit,
                                failSound: () -> Unit,
                                successSound: () -> Unit,
                                winSound: () -> Unit
    ) {
        // one card in play at time
        if (cardInPlay) { // if yet in play, exit
            return
        }
        cardInPlay = true // now card is in play, remember to to revert when necessary

        // check if is a 'false', a discovered couple
        if (_tablePlay.cardsArray[x][y].value.coupled) {
            cardInPlay = false // before exiting the card is not in play anymore
            return
        }

        // first of couple ?
        if (lastMove == noLastMove && !_tablePlay.cardsArray[x][y].value.turned) { // first of couple, show and set card
            lastMove = Pair(x, y)
            flipSound()
            _moves.intValue++
            setTablePlayCardTurned(x = x,y = y, newTurnedState = true)
            cardInPlay = false // before exiting the card is not in play anymore
            return
        } else { // second of couple or second click on first
            // check if is again first of couple to revert
            _moves.intValue++
            if (lastMove == Pair(x, y)) { // first of couple, revert
                _score.value -= (1 + getPointsResetTimeLastMove(0.3f))
                lastMove = noLastMove
                flipSound()
                setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                cardInPlay = false // before exiting the card is not in play anymore
                return
            } else {
                // maybe wrong but show the card for at least a moment
                setTablePlayCardTurned(x = x,y = y, newTurnedState = true)
                // pair found
                if (_tablePlay.cardsArray[lastMove.first][lastMove.second].value.id ==
                    _tablePlay.cardsArray[x][y].value.id
                ) {

                    // TODO
                    _score.value += 12 - getPointsResetTimeLastMove(0.2f)
                    tablePlay.cardsArray[lastMove.first][lastMove.second].value.coupled = true
                    tablePlay.cardsArray[x][y].value.coupled = true
                    // check if it was the last couple -> Game won
                    if(checkAllCardsCoupled()) {
                        winSound()
                        gameWon.value = true
                        setResetPause()
                    } else
                        successSound()
                    lastMove = noLastMove
                    cardInPlay = false // before exiting the card is not in play anymore
                    return
                } else { //no pair found, start a delay than revert the couple
                    _score.value -= (3 + getPointsResetTimeLastMove(0.4f))
                    failSound()
                    viewModelScope.launch(Dispatchers.Default) {
                        delay(1400L)
                        withContext(Dispatchers.Main) {
                            setTablePlayCardTurned(x = lastMove.first,y = lastMove.second, newTurnedState = false)
                            setTablePlayCardTurned(x = x,y = y, newTurnedState = false)
                            lastMove = noLastMove
                            cardInPlay = false // before exiting the card is not in play anymore
                        }
                    }
                }
            }
        }
    }

    private fun setTablePlayCardTurned(x: Int, y: Int, newTurnedState: Boolean) {
        //1.1
        //val oldGameCard = _tablePlay.cardsArray[x][y].value
        //1.2
        //val newGameCard = GameCard(oldGameCard.id, newTurnedState, oldGameCard.coupled)
        //_tablePlay.cardsArray[x][y].value = newGameCard
        //1 -> 2
        //val newGameCard = with(_tablePlay[x][y].value) { GameCard(id, newTurnedState, coupled) }
        //_tablePlay[x][y].value = with(_tablePlay[x][y].value) { GameCard(id, newTurnedState, coupled) }
        _tablePlay.cardsArray[x][y].value = with(_tablePlay.cardsArray[x][y].value) { GameCard(id, newTurnedState, coupled) }
    }

    fun setResetPause() {
        if (!gamePaused.value) {
            gamePaused.value = true
            pauseSimpleTimer()
        } else {
            gamePaused.value = false
            startSimpleTimer()
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

    fun setResetGo() {
        resetGame()
    }

    private fun checkAllCardsCoupled(): Boolean {
        var ret = true
        for(x in (0 until X_BOARD_DIM))
            for(y in (0 until Y_BOARD_DIM)) {
                if (!tablePlay.cardsArray[x][y].value.coupled)
                    ret = false
            }
        return ret
    }

    /**
     * timing functions
     */
    private fun startSimpleTimer() {
        simpleTimerJob?.cancel()
        simpleTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _simpleTimer.value++
            }
        }
    }

    private fun stopSimpleTimer() {
        _simpleTimer.value = 0
        simpleTimerJob?.cancel()
    }

    private fun pauseSimpleTimer() {
        simpleTimerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        simpleTimerJob?.cancel()
    }


    /**
     * get points to be subtracted by times passed from precedent move
     * and reset the counter
     */
    private fun getPointsResetTimeLastMove(multiplier: Float): Int {
        val evaluatedTime = ((_simpleTimer.value - timeOfLastMove) * multiplier).toInt()
        timeOfLastMove = _simpleTimer.value
        return evaluatedTime
    }

}

/**
 * Utility to format a long to time HH:MM:SS string
 */
fun Long.formatSimpleTime(hoursDisplay: Boolean = false): String {
    val hrs = this / 3600
    val mins = (this % 3600) / 60
    val secs = this % 60
    val retStr =
        if (hoursDisplay)
            String.format(java.util.Locale.UK,"%02d:%02d:%02d", hrs, mins, secs)
        else
            if (hrs>0)
                "99:99"
            else
                String.format(java.util.Locale.UK,"%02d:%02d", mins, secs)
    return retStr
}