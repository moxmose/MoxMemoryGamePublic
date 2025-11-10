package com.example.moxmemorygame.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.MutableState
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
import com.example.moxmemorygame.model.GameCard
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
        override val playerName: StateFlow<String> = MutableStateFlow("Test Player"),
        initialBoard: GameBoard? = null,
        initialIsBoardInitialized: State<Boolean> = mutableStateOf(false),
        initialScore: State<Int> = mutableIntStateOf(0),
        initialMoves: State<Int> = mutableIntStateOf(0),
        override val currentTime: MutableStateFlow<Long> = MutableStateFlow(0L),
        initialGamePaused: MutableState<Boolean> = mutableStateOf(false),
        initialGameResetRequest: MutableState<Boolean> = mutableStateOf(false),
        initialGameWon: MutableState<Boolean> = mutableStateOf(false),
        override val selectedBackgrounds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet()),
        override val gameCardImages: List<Int> = emptyList(),
        override val playResetSound: MutableStateFlow<Boolean> = MutableStateFlow(false)
    ) : IGameViewModel {

        override val tablePlay: GameBoard? = initialBoard
        override val isBoardInitialized: State<Boolean> = initialIsBoardInitialized
        override val score: State<Int> = initialScore
        override val moves: State<Int> = initialMoves
        override val gamePaused: MutableState<Boolean> = initialGamePaused
        override val gameResetRequest: MutableState<Boolean> = initialGameResetRequest
        override val gameWon: MutableState<Boolean> = initialGameWon

        var requestPauseDialogCalled by mutableStateOf(false)
            private set
        var requestResetDialogCalled by mutableStateOf(false)
            private set

        private var lastMove: Pair<Int, Int>? = null

        private fun checkWinCondition() {
            val board = tablePlay ?: return
            val allCoupled = board.cardsArray.all { row ->
                row.all { cardState -> cardState.value?.coupled == true }
            }
            if (allCoupled) {
                (gameWon as MutableState<Boolean>).value = true
                (gamePaused as MutableState<Boolean>).value = true // To show the dialog
            }
        }

        override fun checkGamePlayCardTurned(x: Int, y: Int, onSoundEvent: (SoundEvent) -> Unit) {
            val board = tablePlay ?: return
            val card = board.cardsArray[x][y].value ?: return
            if (card.turned || card.coupled) return

            board.cardsArray[x][y].value = card.copy(turned = true)
            (moves as MutableState<Int>).value++

            val lastClicked = lastMove
            if (lastClicked == null) {
                lastMove = x to y
            } else {
                val lastCard = board.cardsArray[lastClicked.first][lastClicked.second].value!!
                if (lastCard.id == card.id) {
                    (score as MutableState<Int>).value += 100
                    board.cardsArray[x][y].value = card.copy(turned = true, coupled = true)
                    board.cardsArray[lastClicked.first][lastClicked.second].value = lastCard.copy(turned = true, coupled = true)
                    checkWinCondition()
                } else {
                    board.cardsArray[x][y].value = card.copy(turned = false)
                    board.cardsArray[lastClicked.first][lastClicked.second].value = lastCard.copy(turned = false)
                }
                lastMove = null
            }
        }

        override fun navigateToOpeningMenuAndCleanupDialogStates() {}
        override fun requestPauseDialog() { requestPauseDialogCalled = true }
        override fun requestResetDialog() { requestResetDialogCalled = true }
        override fun dismissPauseDialog() {}
        override fun cancelResetDialog() {}
        override fun onResetSoundPlayed() {}
    }

    @Test
    fun loadingIndicator_isDisplayed_beforeInitialization() = runTest {
        val fakeViewModel = FakeGameViewModel(initialIsBoardInitialized = mutableStateOf(false))
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_loading_board)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("GameBoard").assertDoesNotExist()
    }

    @Test
    fun gameBoard_isDisplayed_whenInitialized() = runTest {
        val fakeBoard = GameBoard(boardWidth = 2, boardHeight = 2)
        val fakeViewModel = FakeGameViewModel(
            initialBoard = fakeBoard,
            initialIsBoardInitialized = mutableStateOf(true),
            gameCardImages = listOf(1, 2)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        composeTestRule.onNodeWithTag("GameBoard").assertIsDisplayed()
    }

    @Test
    fun pauseDialog_isDisplayed_whenGameIsPaused() = runTest {
        val fakeViewModel = FakeGameViewModel(initialGamePaused = mutableStateOf(true))
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_pause_exit_prompt)).assertIsDisplayed()
    }

    @Test
    fun gameWonDialog_isDisplayed_whenGameIsWon() = runTest {
        val fakeViewModel = FakeGameViewModel(
            initialGamePaused = mutableStateOf(true),
            initialGameWon = mutableStateOf(true)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_won_title)).assertIsDisplayed()
    }

    @Test
    fun resetDialog_isDisplayed_whenResetIsRequested() = runTest {
        val fakeViewModel = FakeGameViewModel(
            initialGamePaused = mutableStateOf(true),
            initialGameResetRequest = mutableStateOf(true)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_reset_title)).assertIsDisplayed()
    }

    @Test
    fun header_displaysCorrectInformation() = runTest {
        val testScore = 123
        val testMoves = 45
        val fakeViewModel = FakeGameViewModel(
            initialScore = mutableIntStateOf(testScore),
            initialMoves = mutableIntStateOf(testMoves)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scoreText = context.getString(R.string.game_head_score, testScore)
        val movesText = context.getString(R.string.game_head_moves, testMoves)
        composeTestRule.onNodeWithText(scoreText).assertIsDisplayed()
        composeTestRule.onNodeWithText(movesText).assertIsDisplayed()
    }

    @Test
    fun clickingPauseButton_requestsPauseDialog() = runTest {
        val fakeViewModel = FakeGameViewModel()
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        composeTestRule.onNodeWithTag("PauseButton").performClick()
        assertThat(fakeViewModel.requestPauseDialogCalled).isTrue()
    }

    @Test
    fun clickingResetButton_requestsResetDialog() = runTest {
        val fakeViewModel = FakeGameViewModel()
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        composeTestRule.onNodeWithTag("ResetButton").performClick()
        assertThat(fakeViewModel.requestResetDialogCalled).isTrue()
    }

    @Test
    fun whenCorrectPairIsClicked_scoreIncreasesAndCardsStayUp() = runTest {
        val board = GameBoard(2, 2).apply {
            cardsArray[0][0].value = GameCard(id = 0, turned = false, coupled = false)
            cardsArray[0][1].value = GameCard(id = 1, turned = false, coupled = false)
            cardsArray[1][0].value = GameCard(id = 1, turned = false, coupled = false)
            cardsArray[1][1].value = GameCard(id = 0, turned = false, coupled = false)
        }
        val fakeViewModel = FakeGameViewModel(
            initialBoard = board,
            initialIsBoardInitialized = mutableStateOf(true),
            gameCardImages = listOf(R.drawable.img_c_01, R.drawable.img_c_02)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }
        composeTestRule.onNodeWithTag("Card_0_0").performClick()
        composeTestRule.onNodeWithTag("Card_1_1").performClick()
        composeTestRule.waitForIdle()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedScoreText = context.getString(R.string.game_head_score, 100)
        composeTestRule.onNodeWithText(expectedScoreText).assertIsDisplayed()
        assertThat(board.cardsArray[0][0].value?.turned).isTrue()
        assertThat(board.cardsArray[0][0].value?.coupled).isTrue()
        assertThat(board.cardsArray[1][1].value?.turned).isTrue()
        assertThat(board.cardsArray[1][1].value?.coupled).isTrue()
    }

    @Test
    fun whenIncorrectPairIsClicked_cardsFlipBack() = runTest {
        val board = GameBoard(2, 2).apply {
            cardsArray[0][0].value = GameCard(id = 0, turned = false, coupled = false)
            cardsArray[0][1].value = GameCard(id = 1, turned = false, coupled = false)
            cardsArray[1][0].value = GameCard(id = 1, turned = false, coupled = false)
            cardsArray[1][1].value = GameCard(id = 0, turned = false, coupled = false)
        }
        val fakeViewModel = FakeGameViewModel(
            initialBoard = board,
            initialIsBoardInitialized = mutableStateOf(true),
            gameCardImages = listOf(R.drawable.img_c_01, R.drawable.img_c_02)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }

        composeTestRule.onNodeWithTag("Card_0_0").performClick()
        composeTestRule.onNodeWithTag("Card_0_1").performClick() // Incorrect pair
        composeTestRule.waitForIdle()

        assertThat(fakeViewModel.moves.value).isEqualTo(2)
        assertThat(board.cardsArray[0][0].value?.turned).isFalse()
        assertThat(board.cardsArray[0][1].value?.turned).isFalse()
    }

    @Test
    fun whenAllPairsAreFound_gameIsWon() = runTest {
        val board = GameBoard(2, 2).apply {
            cardsArray[0][0].value = GameCard(id = 0, turned = false, coupled = false)
            cardsArray[0][1].value = GameCard(id = 1, turned = false, coupled = false)
            cardsArray[1][0].value = GameCard(id = 1, turned = false, coupled = false)
            cardsArray[1][1].value = GameCard(id = 0, turned = false, coupled = false)
        }
        val fakeViewModel = FakeGameViewModel(
            initialBoard = board,
            initialIsBoardInitialized = mutableStateOf(true),
            gameCardImages = listOf(R.drawable.img_c_01, R.drawable.img_c_02)
        )
        composeTestRule.setContent {
            GameScreen(innerPadding = PaddingValues(0.dp), gameViewModel = fakeViewModel)
        }

        // Find all pairs
        composeTestRule.onNodeWithTag("Card_0_0").performClick()
        composeTestRule.onNodeWithTag("Card_1_1").performClick()
        composeTestRule.onNodeWithTag("Card_0_1").performClick()
        composeTestRule.onNodeWithTag("Card_1_0").performClick()
        composeTestRule.waitForIdle()

        // Assert that the game is won and the dialog is shown
        assertThat(fakeViewModel.gameWon.value).isTrue()
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.game_dialog_won_title)).assertIsDisplayed()
    }
}