package com.example.moxmemorygame.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.model.GameBoard
import com.example.moxmemorygame.model.SoundEvent
import com.example.moxmemorygame.ui.IGameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // A fake ViewModel implementing the interface to control the state for tests
    class FakeGameViewModel(
        // We can now define the properties as we need them for the test
        override val playerName: StateFlow<String> = MutableStateFlow("Test Player"),
        override val tablePlay: GameBoard? = null,
        override val isBoardInitialized: State<Boolean> = mutableStateOf(false),
        override val score: State<Int> = mutableIntStateOf(0),
        override val moves: State<Int> = mutableIntStateOf(0),
        override val currentTime: MutableStateFlow<Long> = MutableStateFlow(0L),
        override val gamePaused: State<Boolean> = mutableStateOf(false),
        override val gameResetRequest: State<Boolean> = mutableStateOf(false),
        override val gameWon: State<Boolean> = mutableStateOf(false),
        override val selectedBackgrounds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet()),
        override val gameCardImages: List<Int> = emptyList(),
        override val playResetSound: MutableStateFlow<Boolean> = MutableStateFlow(false)
    ) : IGameViewModel {
        // Implement methods with dummy logic, we only care about state for this UI test
        override fun checkGamePlayCardTurned(x: Int, y: Int, onSoundEvent: (SoundEvent) -> Unit) {}
        override fun navigateToOpeningMenuAndCleanupDialogStates() {}
        override fun requestPauseDialog() {}
        override fun requestResetDialog() {}
        override fun dismissPauseDialog() {}
        override fun cancelResetDialog() {}
        override fun onResetSoundPlayed() {}
    }

    @Test
    fun gameBoard_isDisplayed_whenInitialized() = runTest {
        // 1. Prepare the fake state
        val fakeBoard = GameBoard(boardWidth = 2, boardHeight = 2)
        val fakeViewModel = FakeGameViewModel(
            tablePlay = fakeBoard,
            isBoardInitialized = mutableStateOf(true)
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Assert that the board is displayed
        composeTestRule.onNodeWithTag("GameBoard").assertIsDisplayed()
    }
}