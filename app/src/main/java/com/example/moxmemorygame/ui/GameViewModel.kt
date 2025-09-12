package com.example.moxmemorygame.ui

import android.util.Log // Aggiunto per i log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.IAppSettingsDataStore
import com.example.moxmemorygame.RealAppSettingsDataStore // Per fallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first // Aggiunto per .first()
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val navController: NavHostController,
    private val timerViewModel: TimerViewModel,
    private val appSettingsDataStore: IAppSettingsDataStore,
    private val resourceNameToId: (String) -> Int // Iniezione della lambda per conversione nome->ID
): ViewModel() {
    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    // selectedCards è usato in loadAndShuffleCards, ma non esposto direttamente se non necessario
    // val selectedCards: StateFlow<Set<String>> = appSettingsDataStore.selectedCards
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

    private val _tablePlay = GameCardArray() // GameCard.id rimane Int
    val tablePlay: GameCardArray get() = _tablePlay

    @DrawableRes
    private lateinit var _gameCardImages: List<Int> // Rimane List<Int>, conterrà gli ID risorsa reali
    val gameCardImages get() = _gameCardImages

    val currentTime = timerViewModel.elapsedSeconds
    private var timeOfLastMove = 0L

    var gamePaused: MutableState<Boolean> = mutableStateOf(false)
    var gameResetRequest: MutableState<Boolean> = mutableStateOf(false)
    var gameWon: MutableState<Boolean> = mutableStateOf(false)

    init {
        Log.d("GameVM", "init - Calling resetGame()")
        resetGame() // resetGame ora lancia una coroutine
    }

    private fun resetGame() {
        Log.d("GameVM", "resetGame - Starting game reset process")
        viewModelScope.launch {
            Log.d("GameVM", "resetGame - Coroutine launched, calling loadAndShuffleCards()")
            loadAndShuffleCards() // Carica e mescola le carte basate sulle preferenze
            
            Log.d("GameVM", "resetGame - loadAndShuffleCards() completed, proceeding with main thread state reset")
            // Il resto del reset avviene sul thread principale dopo il caricamento delle carte
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

    // Sostituisce la vecchia resetAndShuffleImages()
    private suspend fun loadAndShuffleCards() {
        Log.d("GameVM", "loadAndShuffleCards - Starting to load and shuffle cards")
        val uniqueCardsNeeded = (BOARD_WIDTH * BOARD_HEIGHT) / 2
        var userSelectedResourceNames = appSettingsDataStore.selectedCards.first()
        Log.d("GameVM", "loadAndShuffleCards - Read from DataStore: $userSelectedResourceNames")

        // Fallback robusto: se le carte selezionate sono insufficienti (improbabile data la logica in PreferencesViewModel)
        // o se il set di default è l'unica fonte, assicurati di avere abbastanza carte.
        if (userSelectedResourceNames.size < uniqueCardsNeeded) {
            Log.w("GameVM", "loadAndShuffleCards - User selected cards insufficient (${userSelectedResourceNames.size} < $uniqueCardsNeeded). Falling back to default.")
            userSelectedResourceNames = RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS
        }
        
        // Prende il numero corretto di nomi di risorse uniche, mescolandole se ce ne sono di più.
        val actualCardResourceNamesForGame = userSelectedResourceNames.shuffled().take(uniqueCardsNeeded)
        Log.d("GameVM", "loadAndShuffleCards - Actual card resource names for game: $actualCardResourceNamesForGame")

        // Converti i nomi delle risorse selezionate in ID risorsa reali usando la lambda iniettata
        // _gameCardImages conterrà gli ID @DrawableRes effettivi.
        _gameCardImages = actualCardResourceNamesForGame.map { resourceName -> resourceNameToId(resourceName) }
        Log.d("GameVM", "loadAndShuffleCards - Converted to resource IDs (_gameCardImages): $_gameCardImages")

        // Crea gli ID logici per le GameCard (da 0 a uniqueCardsNeeded - 1)
        // Questi ID logici verranno usati come indici per _gameCardImages in GamePlayScreen
        val logicalCardIds = (0 until uniqueCardsNeeded).toList()
        val gameCardLogicalIdsForBoard = (logicalCardIds + logicalCardIds).shuffled()
        Log.d("GameVM", "loadAndShuffleCards - Prepared logical IDs for board: $gameCardLogicalIdsForBoard")

        // Popola il tabellone con GameCard che usano gli ID logici
        // Questo deve avvenire sul thread principale se _tablePlay.cardsArray[x][y] è MutableState
        // o se l'aggiornamento dell'UI dipende da queste modifiche immediatamente.
        withContext(Dispatchers.Main) {
            var i = 0
            for(x in (0 until BOARD_WIDTH)) {
                for(y in (0 until BOARD_HEIGHT)) {
                     if (i < gameCardLogicalIdsForBoard.size) { // Controllo di sicurezza
                        _tablePlay.cardsArray[x][y].value = GameCard(
                            id = gameCardLogicalIdsForBoard[i++], // Usa l'ID logico
                            turned = false,
                            coupled = false
                        )
                    } else {
                        // Questo non dovrebbe accadere se uniqueCardsNeeded * 2 == BOARD_WIDTH * BOARD_HEIGHT
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
                                pauseSound: () -> Unit, // parametro non usato, ma mantenuto
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
                // Il confronto degli ID rimane tra Int (ID logici)
                if (_tablePlay.cardsArray[lastMove.first][lastMove.second].value.id ==
                    _tablePlay.cardsArray[x][y].value.id
                ) {
                    refreshPointsRightCouple()
                    // Ripristinato all'assegnazione diretta come indicato dall'utente
                    _tablePlay.cardsArray[lastMove.first][lastMove.second].value.coupled = true
                    _tablePlay.cardsArray[x][y].value.coupled = true
                    
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
        // Assumendo che GameCard abbia un metodo copy o sia una data class e abbia un .copy(turned = newTurnedState)
        // Se value.copyChangingTurned è un tuo metodo di estensione, va bene così.
        // Altrimenti, se GameCard è una data class: _tablePlay.cardsArray[x][y].value = _tablePlay.cardsArray[x][y].value.copy(turned = newTurnedState)
        // Mantengo il tuo codice originale qui:
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
        resetGame() // Chiama la nuova resetGame che gestisce il caricamento asincrono
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
