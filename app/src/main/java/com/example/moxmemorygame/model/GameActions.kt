package com.example.moxmemorygame.model

/**
 * Represents all possible user actions from the game screen's footer/tail section.
 * Using a sealed class allows for exhaustive checks in `when` statements.
 */
sealed class GameFooterAction {
    object Pause : GameFooterAction()
    object Reset : GameFooterAction()
}
