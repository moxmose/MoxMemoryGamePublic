package com.example.moxmemorygame.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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

class GameViewModel(private val timerViewModel: TimerViewModel): ViewModel() {
    private val _score = mutableIntStateOf(0)   // player's actual score
    val score get() = _score

    private val _moves = mutableIntStateOf(0)   // player's actual moves
    val moves get() = _moves

    private var _gamePlayResetSound = mutableStateOf(false) // play reset Sound
    val gamePlayResetSound get() = _gamePlayResetSound.value

    private var lastMove: Pair<Int, Int> = Pair(0,0)    // coordinates of last card clicked
    private val noLastMove = Pair(-1, -1)   // Used as standard value for previous move
    private var cardInPlay: Boolean = false // true if there is a actual selected card

//    private var gameFinished: Boolean = false   // game finished, stop timer and show score

    private val _tablePlay = GameCardArray()    // array of cards arranged in a table
    val tablePlay: GameCardArray get() = _tablePlay

    @DrawableRes
    private lateinit var _gameCardImages: List<Int> // images of cards
    val gameCardImages get() = _gameCardImages

    // Timing variables
    val currentTime = timerViewModel.elapsedSeconds
    private var timeOfLastMove = 0L

    var gamePaused: MutableState<Boolean> = mutableStateOf(false)
    var gameResetRequest: MutableState<Boolean> = mutableStateOf(false)
    var gameWon: MutableState<Boolean> = mutableStateOf(false)

    init {
        resetGame()
    }

    /**
     * Resets the game to its initial state.
     *
     * This function performs the following actions:
     *   - Resets and shuffles the game images using `resetAndShuffleImages()`.
     *   - Sets the score to 0.
     *   - Sets the number of moves to 0.
     *   - Clears the last move information.
     *   - Resets various game state flags (cardInPlay, gameStarted, gameFinished, gamePaused, gameResetRequest, gameWon).
     *   - Stops the current timer and starts a new one.
     *   - Resets the time of the last move.
     */
    private fun resetGame() {
        resetAndShuffleImages()
        _score.intValue = 0
        _moves.intValue = 0
        lastMove = Pair(-1,-1)
        cardInPlay = false
        _gamePlayResetSound.value = true
    //    gameStarted = false
    //    gameFinished = false
        gamePaused.value = false
        gameResetRequest.value = false
        gameWon.value = false
        timerViewModel.resetTimer()
        timerViewModel.startTimer()
        timeOfLastMove = 0L
    }

    /**
     * Resets the game board and shuffles the images to be used for the game cards.
     *
     * This function performs the following steps:
     * 1. **Determines the number of unique image pairs:** It calculates the number of unique image pairs needed for the game based on the board dimensions (X_BOARD_DIM and Y_BOARD_DIM).  Each unique image will have two corresponding cards on the board.
     * 2. **Shuffles and selects images:** It takes a shuffled list of available images (from `GameCardImages().image`) and selects the required number of unique images to be used in the current game. The selected images are stored in `_gameCardImages`.
     * 3. **Creates shuffled card indexes:** It creates a list containing pairs of indexes. Each index represents a unique image. This list includes each index twice, and it is then shuffled to randomize the positions of the image pairs on the board.
     * 4. **Assigns values to game cards:** It iterates through each cell of the game board (`_tablePlay.cardsArray`) and assigns a `GameCard` object to it.
     *   - **id:** The `id` of each card is taken from the shuffled `gameCardIndexes` list. This `id` corresponds to a specific image in the `_gameCardImages` list and ensures that each image appears twice on the board.
     *   - **turned:** Initially, all cards are set to `turned = false`, meaning they are face down.
     *   - **coupled:** Initially, all cards are set to `coupled = false`, meaning no pairs have been found yet.
     *
     * After this function completes, the game board is initialized with shuffled images, and all cards are face down and not yet coupled.
     */
    private fun resetAndShuffleImages() {
        val dim = BOARD_WIDTH * BOARD_HEIGHT /2 -1   // dimension of the board from element 0
        _gameCardImages = GameCardImages().image.shuffled().subList(0,dim+1)    //set of shuffled images used for actual game

        val gameCardIndexes = ((0..dim) + (0..dim)).shuffled()  //shuffled set of values from 0 to dim, twice the values
        var i = 0
        for(x in (0 until BOARD_WIDTH))
            for(y in (0 until BOARD_HEIGHT))
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
        cardInPlay = true // from now card is in play, remember to to revert when necessary

        // check if is a 'false', a click on a discovered couple
        if (_tablePlay.cardsArray[x][y].value.coupled) {
            cardInPlay = false // before exiting the card is not in play anymore
            return
        }

        // is the first of a couple?
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
                refreshPointsNoCoupleSelected()
                //_score.value -= (1 + getPointsResetTimeLastMove(0.3f))
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

                    refreshPointsRightCouple()
                    //_score.value += 12 - getPointsResetTimeLastMove(0.2f)
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
                    refreshPointsWrongCouple()
                    //_score.value -= (3 + getPointsResetTimeLastMove(0.4f))
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

    /**
     * Sets the turned state of a card on the table.
     *
     * This function updates the `turned` property of a specific card within the `_tablePlay.cardsArray`.
     * It identifies the target card using the provided `x` and `y` coordinates (representing row and column indices).
     * It then updates the card's `turned` state to the given `newTurnedState` while preserving its existing `id` and `coupled` properties.
     * The update is performed using the `copyChangingTurned` method on the `GameCard` object.
     *
     * The `_tablePlay.cardsArray` is treated as a 2D array (list of lists), where:
     * - `x` represents the row index.
     * - `y` represents the column index within that row.
     *
     * @param x The x-coordinate (row index) of the card on the table. Must be within the valid range of row indices for `_tablePlay.cardsArray`.
     * @param y The y-coordinate (column index) of the card on the table. Must be within the valid range of column indices for the row `x` in `_tablePlay.cardsArray`.
     * @param newTurnedState The new turned state of the card. `true` indicates the card should be turned, `false` indicates it should not be turned.
     */
    private fun setTablePlayCardTurned(x: Int, y: Int, newTurnedState: Boolean) {
        // Validate input coordinates in a strict constriction
        require(x in _tablePlay.cardsArray.indices) { "x coordinate is out of bounds" }
        require(y in _tablePlay.cardsArray[x].indices) { "y coordinate is out of bounds" }
        //different implementations:
        //1.1
        //val oldGameCard = _tablePlay.cardsArray[x][y].value
        //1.2
        //val newGameCard = GameCard(oldGameCard.id, newTurnedState, oldGameCard.coupled)
        //_tablePlay.cardsArray[x][y].value = newGameCard
        //1 -> 2
        //val newGameCard = with(_tablePlay[x][y].value) { GameCard(id, newTurnedState, coupled) }
        //_tablePlay[x][y].value = with(_tablePlay[x][y].value) { GameCard(id, newTurnedState, coupled) }
        //_tablePlay.cardsArray[x][y].value = with(_tablePlay.cardsArray[x][y].value) { GameCard(id, newTurnedState, coupled) }
        //_tablePlay.cardsArray[x][y].value = _tablePlay.cardsArray[x][y].value.copyChangingTurned(newTurnedState)
        with(_tablePlay.cardsArray[x][y]) { value = value.copyChangingTurned(newTurnedState) }
    }

    /**
     * Toggles the game's pause/resume state and manages the associated timer.
     *
     * This function acts as a pause/resume toggle for the game. When called, it inverts the current
     * pause state and updates the game timer accordingly.
     *
     * - **Pausing:** If the game is running (not paused), this function will:
     *   1. Set `gamePaused` to `true`.
     *   2. Pause the game timer using `timerViewModel.pauseTimer()`.
     *
     * - **Resuming:** If the game is paused, this function will:
     *   1. Set `gamePaused` to `false`.
     *   2. Resume the game timer using `timerViewModel.resumeTimer()`.
     *
     * This effectively controls both the game's logic flow (via `gamePaused`) and its timer.
     *
     * **Note:** This function only manages pausing and resuming. It does not handle game or timer reset.
     */
    fun setResetPause() {
        if (!gamePaused.value) {
            gamePaused.value = true
            timerViewModel.pauseTimer()
        } else {
            gamePaused.value = false
            timerViewModel.resumeTimer()
        }
    }

    /**
     * Toggles the game reset request state and triggers a pause reset.
     *
     * This function manages a boolean flag `gameResetRequest` which indicates whether a game reset has been requested.
     * It inverts the current state of `gameResetRequest` and then calls `setResetPause()` to handle
     * any necessary pause-related actions associated with the reset.
     *
     * Specifically:
     * - If `gameResetRequest` is currently `false` (no reset requested), it sets it to `true` (reset requested)
     *   and then calls `setResetPause()`.
     * - If `gameResetRequest` is currently `true` (reset requested), it sets it to `false` (reset request cancelled)
     *   and then calls `setResetPause()`.
     *
     * The `setResetPause()` function is responsible for handling the pause state potentially associated with a game reset.
     * This could include pausing the game, showing a reset confirmation screen, or any other relevant actions.
     *
     * Note: The `gameResetRequest` variable is assumed to be a `MutableLiveData<Boolean>` or similar observable type
     * that can hold a boolean value and trigger UI updates when its value changes.
     *
     * @see setResetPause
     */
    fun setResetReset() {
        if(!gameResetRequest.value) {
            gameResetRequest.value = true
            setResetPause()
        } else {
            gameResetRequest.value = false
            setResetPause()
        }
    }

    /**
     * Resets the game to its initial state and allows it to proceed.
     *
     * Calls [resetGame] to perform the reset operation.
     */
    fun resetProceed() {
        resetGame()
    }

    /**
     * Sets the value of the `_gamePlayResetSound` LiveData to `true`, signaling that the game play
     * reset sound should be played. This function is called both when the game is first started
     * and when the game state is reset. It triggers the playback of a sound effect associated
     * with either the initial setup or a reset action.
     */
    fun setPlayResetSound() {
        _gamePlayResetSound.value = true

    }

    /**
     * Plays the game reset sound and resets the internal sound played state.
     *
     * Executes the provided `resetSound` lambda to play the sound effect and then sets
     * `_gamePlayResetSound` to `false`, allowing the sound to be played again on the next reset.
     *
     * @param resetSound Lambda to play the reset sound.
     */
    fun resetPlayResetSound(resetSound: () -> Unit) {
        resetSound()
        _gamePlayResetSound.value = false
    }


    /**
     * Checks if all cards on the game board are coupled.
     *
     * This function iterates through all cards in the `tablePlay.cardsArray` (which represents the game board)
     * and verifies if each card's `coupled` property is true.
     *
     * A card is considered "coupled" if it has been matched with another card of the same value.
     * This function efficiently checks this condition for all cards.
     *
     * The implementation utilizes a flattened representation of the 2D array `tablePlay.cardsArray`
     * for easier iteration and leverages the `all` higher-order function for a concise check.
     *
     * @return `true` if all cards on the board are coupled (i.e., their `coupled` property is true),
     *         `false` otherwise.
     *
     * @see tablePlay
     * @see tablePlay.cardsArray
     * @see Card.coupled
     */
    private fun checkAllCardsCoupled(): Boolean {
        //old classical implementation
        // 1.
        //var ret = true
        //for(x in (0 until BOARD_WIDTH))
        //    for(y in (0 until BOARD_HEIGHT)) {
        //        if (!tablePlay.cardsArray[x][y].value.coupled)
        //            ret = false
        //    }
        //return ret
        // 2.
        //return tablePlay.cardsArray.all { row -> row.all { it.value.coupled } }
        return tablePlay.cardsArray.flatten().all { it.value.coupled }
    }

    /**
     * get points to be subtracted by times passed from precedent move
     * and reset the counter
     */
    private fun getPointsResetTimeLastMove(multiplier: Float): Int {
        val timerPoints = ((currentTime.value - timeOfLastMove) * multiplier).toInt()
        timeOfLastMove = currentTime.value
        return timerPoints
    }

    private fun refreshPointsWrongCouple() {_score.value -= (3 + getPointsResetTimeLastMove(0.4f))}
    private fun refreshPointsRightCouple() {_score.value += (12 - getPointsResetTimeLastMove(0.2f))}
    private fun refreshPointsNoCoupleSelected() {_score.value -= (1 + getPointsResetTimeLastMove(0.5f))}

    /**
     * This ViewModel's `onCleared` implementation.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            timerViewModel.stopAndAwaitTimerCompletion()
        }
    }

}

