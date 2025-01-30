package com.example.moxmemorygame.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource

// dimension of the play base
const val X_BOARD_DIM = 4
const val Y_BOARD_DIM = 5

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
        val cardsArray: Array<Array<MutableState<GameCard>>> = Array(X_BOARD_DIM) {
        Array(Y_BOARD_DIM) {
            mutableStateOf(GameCard(id = it, turned = false, coupled = false))
        }
    }
)

