package com.example.moxmemorygame.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

// dimension of the play base (these might be used as defaults or max/min elsewhere)
const val BOARD_WIDTH = 4 
const val BOARD_HEIGHT = 5

/** GameCard is the base of a card identity
 */
data class GameCard(
    val id: Int,
    val turned: Boolean,
    val coupled: Boolean
)

/** GameBoard is the base of array of cards
 *  Now accepts dynamic width and height.
 */
class GameBoard(
    val boardWidth: Int, // Parametro per la larghezza
    val boardHeight: Int // Parametro per l'altezza
) {
    val cardsArray: Array<Array<MutableState<GameCard>>> = Array(boardWidth) {
        Array(boardHeight) {
            // L'ID iniziale qui è un placeholder, verrà sovrascritto in GameViewModel
            mutableStateOf(GameCard(id = -1, turned = false, coupled = false)) 
        }
    }
}
