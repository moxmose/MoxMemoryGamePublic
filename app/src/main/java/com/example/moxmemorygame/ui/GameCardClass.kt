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

/**
 * Creates a new [GameCard] instance with the same `id` and `coupled` properties as the original,
 * but with a potentially different `turned` state.
 *
 * This function provides a convenient way to create a modified copy of a [GameCard]
 * where only the `turned` property is changed. It's useful in scenarios where
 * the state of a card needs to be updated without altering its identity or
 * coupling status.
 *
 * @param newTurned The new value for the `turned` property of the copied card.
 * @return A new [GameCard] instance with the specified `turned` state and the same `id` and `coupled`
 *         as the original card.
 */
fun GameCard.copyChangingTurned(newTurned: Boolean): GameCard {
    return GameCard(
        id = this.id,
        turned = newTurned,
        coupled = this.coupled
    )
}

