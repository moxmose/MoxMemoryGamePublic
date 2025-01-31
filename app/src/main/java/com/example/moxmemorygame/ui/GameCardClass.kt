package com.example.moxmemorygame.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

// dimension of the play base
const val BOARD_WIDTH = 4
const val BOARD_HEIGHT = 5

/** GameCard is the base of a card identity
 */
class GameCard(
    val id: Int,
    var turned: Boolean,
    var coupled: Boolean
)

/** GameCardArray is the base of array of cards
 */
class GameCardArray(
        val cardsArray: Array<Array<MutableState<GameCard>>> = Array(BOARD_WIDTH) {
        Array(BOARD_HEIGHT) {
            mutableStateOf(GameCard(id = it, turned = false, coupled = false))
        }
    }
)

