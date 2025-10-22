package com.example.moxmemorygame.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

// Default dimensions for the game board, used as fallbacks.
const val BOARD_WIDTH = 4 
const val BOARD_HEIGHT = 5

/**
 * Represents a single card in the game.
 * @param id The unique identifier for the card's face.
 * @param turned Whether the card is currently face-up.
 * @param coupled Whether the card has been successfully matched with its pair.
 */
data class GameCard(
    val id: Int,
    val turned: Boolean,
    val coupled: Boolean
)

/**
 * Represents the game board, holding the state of all cards.
 * @param boardWidth The width of the board in cells.
 * @param boardHeight The height of the board in cells.
 */
class GameBoard(
    val boardWidth: Int, 
    val boardHeight: Int
) {
    val cardsArray: Array<Array<MutableState<GameCard>>> = Array(boardWidth) {
        Array(boardHeight) {
            // The initial ID is a placeholder and will be overwritten by the ViewModel.
            mutableStateOf(GameCard(id = -1, turned = false, coupled = false)) 
        }
    }
}
