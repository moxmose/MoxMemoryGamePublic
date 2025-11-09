package com.example.moxmemorygame.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.moxmemorygame.R
import com.example.moxmemorygame.model.GameBoard
import com.example.moxmemorygame.model.SoundEvent
import com.example.moxmemorygame.ui.IGameViewModel
import com.google.common.truth.Truth.assertThat
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

        var requestPauseDialogCalled by mutableStateOf(false)
            private set
        var requestResetDialogCalled by mutableStateOf(false)
            private set
        var lastClickedX by mutableIntStateOf(-1)
            private set
        var lastClickedY by mutableIntStateOf(-1)
            private set

        // Implement methods with dummy logic, we only care about state for this UI test
        override fun checkGamePlayCardTurned(x: Int, y: Int, onSoundEvent: (SoundEvent) -> Unit) {
            lastClickedX = x
            lastClickedY = y
        }
        override fun navigateToOpeningMenuAndCleanupDialogStates() {}
        override fun requestPauseDialog() {
            requestPauseDialogCalled = true
        }
        override fun requestResetDialog() {
            requestResetDialogCalled = true
        }
        override fun dismissPauseDialog() {}
        override fun cancelResetDialog() {}
        override fun onResetSoundPlayed() {}
    }

    @Test
    fun loadingIndicator_isDisplayed_beforeInitialization() = runTest {
        // 1. Prepare the fake state
        val fakeViewModel = FakeGameViewModel(
            isBoardInitialized = mutableStateOf(false)
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Assert that the loading text is displayed
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_loading_board)).assertIsDisplayed()

        // 4. Assert that the board is NOT displayed
        composeTestRule.onNodeWithTag("GameBoard").assertDoesNotExist()
    }

    @Test
    fun gameBoard_isDisplayed_whenInitialized() = runTest {
        // 1. Prepare the fake state
        val fakeBoard = GameBoard(boardWidth = 2, boardHeight = 2)
        val fakeViewModel = FakeGameViewModel(
            tablePlay = fakeBoard,
            isBoardInitialized = mutableStateOf(true),
            gameCardImages = listOf(1, 2) // Provide a non-empty list of card images
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

    @Test
    fun pauseDialog_isDisplayed_whenGameIsPaused() = runTest {
        // 1. Prepare the fake state
        val fakeViewModel = FakeGameViewModel(
            gamePaused = mutableStateOf(true)
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Assert that the pause dialog is displayed
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_pause_exit_prompt)).assertIsDisplayed()
    }

    @Test
    fun gameWonDialog_isDisplayed_whenGameIsWon() = runTest {
        // 1. Prepare the fake state
        val fakeViewModel = FakeGameViewModel(
            gamePaused = mutableStateOf(true),
            gameWon = mutableStateOf(true)
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Assert that the game won dialog is displayed
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_won_title)).assertIsDisplayed()
    }

    @Test
    fun resetDialog_isDisplayed_whenResetIsRequested() = runTest {
        // 1. Prepare the fake state
        val fakeViewModel = FakeGameViewModel(
            gamePaused = mutableStateOf(true),
            gameResetRequest = mutableStateOf(true)
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Assert that the reset dialog is displayed
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_reset_title)).assertIsDisplayed()
    }

    @Test
    fun header_displaysCorrectInformation() = runTest {
        // 1. Prepare the fake state
        val testScore = 123
        val testMoves = 45
        val fakeViewModel = FakeGameViewModel(
            score = mutableIntStateOf(testScore),
            moves = mutableIntStateOf(testMoves)
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Assert that the header information is displayed correctly
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scoreText = context.getString(R.string.game_head_score, testScore)
        val movesText = context.getString(R.string.game_head_moves, testMoves)

        composeTestRule.onNodeWithText(scoreText).assertIsDisplayed()
        composeTestRule.onNodeWithText(movesText).assertIsDisplayed()
    }

    @Test
    fun clickingPauseButton_requestsPauseDialog() = runTest {
        // 1. Prepare the fake state
        val fakeViewModel = FakeGameViewModel()

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Click the pause button
        composeTestRule.onNodeWithTag("PauseButton").performClick()

        // 4. Assert that the view model's method was called
        assertThat(fakeViewModel.requestPauseDialogCalled).isTrue()
    }

    @Test
    fun clickingResetButton_requestsResetDialog() = runTest {
        // 1. Prepare the fake state
        val fakeViewModel = FakeGameViewModel()

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Click the reset button
        composeTestRule.onNodeWithTag("ResetButton").performClick()

        // 4. Assert that the view model's method was called
        assertThat(fakeViewModel.requestResetDialogCalled).isTrue()
    }

    @Test
    fun clickingCard_callsViewModel() = runTest {
        // 1. Prepare the fake state
        val boardWidth = 2
        val boardHeight = 2
        val fakeBoard = GameBoard(boardWidth = boardWidth, boardHeight = boardHeight)
        val fakeViewModel = FakeGameViewModel(
            tablePlay = fakeBoard,
            isBoardInitialized = mutableStateOf(true),
            gameCardImages = listOf(1, 2, 3, 4) 
        )

        // 2. Set the content
        composeTestRule.setContent {
            GameScreen(
                innerPadding = PaddingValues(0.dp),
                gameViewModel = fakeViewModel
            )
        }

        // 3. Click a card
        val x = 1
        val y = 1
        composeTestRule.onNodeWithTag("Card_${x}_${y}").performClick()

        // 4. Assert that the view model's method was called with the correct coordinates
        assertThat(fakeViewModel.lastClickedX).isEqualTo(x)
        assertThat(fakeViewModel.lastClickedY).isEqualTo(y)
    }
}
