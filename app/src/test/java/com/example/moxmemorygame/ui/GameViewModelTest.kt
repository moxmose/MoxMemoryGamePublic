package com.example.moxmemorygame.ui

import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.GameCard
import com.example.moxmemorygame.model.SoundEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q]) // Specify the SDK to simulate
class GameViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    private lateinit var viewModel: GameViewModel
    private lateinit var fakeDataStore: FakeAppSettingsDataStore
    private lateinit var testNavController: TestNavHostController
    private lateinit var fakeTimerViewModel: FakeTimerViewModel

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        fakeDataStore = FakeAppSettingsDataStore()
        fakeTimerViewModel = FakeTimerViewModel()

        // Use the TestNavHostController
        testNavController = TestNavHostController(ApplicationProvider.getApplicationContext())
        testNavController.navigatorProvider.addNavigator(ComposeNavigator())
        testNavController.graph = testNavController.createGraph(startDestination = "game_screen") {
            composable("game_screen") { }
            composable(Screen.OpeningMenuScreen.route) { }
        }
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = GameViewModel(
            navController = testNavController,
            timerViewModel = fakeTimerViewModel,
            appSettingsDataStore = fakeDataStore,
            resourceNameToId = { 0 },
            ioDispatcher = testDispatcher, // Use the test dispatcher for IO operations
            delayProvider = { _ -> } // No delay in tests
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun initialStateIsCorrect() = runTest(testDispatcher) {
        advanceUntilIdle()

        val expectedWidth = IAppSettingsDataStore.DEFAULT_BOARD_WIDTH
        val expectedHeight = IAppSettingsDataStore.DEFAULT_BOARD_HEIGHT

        assertThat(viewModel.isBoardInitialized.value).isTrue()
        assertThat(viewModel.moves.intValue).isEqualTo(0)
        assertThat(viewModel.score.intValue).isEqualTo(0)
        assertThat(viewModel.tablePlay).isNotNull()
        assertThat(viewModel.tablePlay!!.boardWidth).isEqualTo(expectedWidth)
        assertThat(viewModel.tablePlay!!.boardHeight).isEqualTo(expectedHeight)
        assertThat(viewModel.tablePlay!!.cardsArray.sumOf { it.size }).isEqualTo(expectedWidth * expectedHeight)
        assertThat(viewModel.playResetSound.value).isTrue() // Check that reset sound is requested
    }

    @Test
    fun checkGamePlayCardTurnedWhenFirstCardIsTurnedUpdatesStateCorrectly() = runTest(testDispatcher) {
        // Arrange
        advanceUntilIdle() // Ensure initial setup is complete
        var triggeredSound: SoundEvent? = null
        val onSoundEvent = { event: SoundEvent -> triggeredSound = event }

        val cardBeforeTurn = viewModel.tablePlay!!.cardsArray[0][0].value
        assertThat(cardBeforeTurn.turned).isFalse() // Pre-condition

        // Act
        viewModel.checkGamePlayCardTurned(x = 0, y = 0, onSoundEvent = onSoundEvent)

        // Assert
        val cardAfterTurn = viewModel.tablePlay!!.cardsArray[0][0].value
        assertThat(cardAfterTurn.turned).isTrue()
        assertThat(viewModel.moves.intValue).isEqualTo(1)
        assertThat(triggeredSound).isEqualTo(SoundEvent.Flip)
    }

    @Test
    fun checkGamePlayCardTurnedWhenCorrectPairIsTurnedUpdatesScoreAndState() = runTest(testDispatcher) {
        // Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        val (card1Pos, card2Pos) = findPairOnBoard(board.cardsArray)
        val initialScore = viewModel.score.intValue
        val triggeredSounds = mutableListOf<SoundEvent>()
        val onSoundEvent: (SoundEvent) -> Unit = { event: SoundEvent -> triggeredSounds.add(event) }

        // Act
        viewModel.checkGamePlayCardTurned(card1Pos.first, card1Pos.second, onSoundEvent)
        fakeTimerViewModel.setElapsedTime(2) // Simulate 2 seconds passing
        viewModel.checkGamePlayCardTurned(card2Pos.first, card2Pos.second, onSoundEvent)
        advanceUntilIdle()

        // Assert
        val card1 = board.cardsArray[card1Pos.first][card1Pos.second].value
        val card2 = board.cardsArray[card2Pos.first][card2Pos.second].value

        assertThat(card1.coupled).isTrue()
        assertThat(card2.coupled).isTrue()
        assertThat(viewModel.score.intValue).isGreaterThan(initialScore)
        assertThat(viewModel.moves.intValue).isEqualTo(2)
        assertThat(triggeredSounds).contains(SoundEvent.Success)
    }

    @Test
    fun checkGamePlayCardTurnedWhenIncorrectPairIsTurnedFlipsThemBack() = runTest(testDispatcher) {
        // Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        val (card1Pos, card2Pos) = findNonPairOnBoard(board.cardsArray)
        val triggeredSounds = mutableListOf<SoundEvent>()
        val onSoundEvent: (SoundEvent) -> Unit = { event: SoundEvent -> triggeredSounds.add(event) }

        // Act
        viewModel.checkGamePlayCardTurned(card1Pos.first, card1Pos.second, onSoundEvent)
        fakeTimerViewModel.setElapsedTime(2) // Simulate 2 seconds passing
        viewModel.checkGamePlayCardTurned(card2Pos.first, card2Pos.second, onSoundEvent)

        advanceUntilIdle()

        // Assert
        val card1 = board.cardsArray[card1Pos.first][card1Pos.second].value
        val card2 = board.cardsArray[card2Pos.first][card2Pos.second].value

        assertThat(card1.turned).isFalse()
        assertThat(card2.turned).isFalse()
        assertThat(triggeredSounds).contains(SoundEvent.Fail)
    }

    @Test
    fun checkGamePlayCardTurnedWhenLastPairIsFoundTriggersWinCondition() = runTest(testDispatcher) {
        // Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        val (lastPairCard1, lastPairCard2) = setupAlmostWonBoard(board.cardsArray)
        viewModel.score.intValue = 900 // Simulate an existing score
        val triggeredSounds = mutableListOf<SoundEvent>()
        val onSoundEvent: (SoundEvent) -> Unit = { event: SoundEvent -> triggeredSounds.add(event) }

        // Act
        viewModel.checkGamePlayCardTurned(lastPairCard1.first, lastPairCard1.second, onSoundEvent)
        fakeTimerViewModel.setElapsedTime(2) // Simulate 2 seconds passing
        viewModel.checkGamePlayCardTurned(lastPairCard2.first, lastPairCard2.second, onSoundEvent)
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.gameWon.value).isTrue()
        assertThat(fakeTimerViewModel.isTimerRunning()).isFalse()
        assertThat(triggeredSounds).contains(SoundEvent.Win)
        assertThat(viewModel.gamePaused.value).isTrue() // Verify that the pause/win dialog is requested

        // Check that the final score was saved
        val finalScore = viewModel.score.intValue
        assertThat(fakeDataStore.lastPlayedEntry.value?.score).isEqualTo(finalScore)
    }

    @Test
    fun resetCurrentGameResetsAllGameState() = runTest(testDispatcher) {
        // Arrange: Simulate a game in progress
        advanceUntilIdle()
        viewModel.score.intValue = 500
        viewModel.moves.intValue = 10
        fakeTimerViewModel.setElapsedTime(60) // 1 minute passed
        viewModel.gameWon.value = true // Simulate a won state to ensure it gets reset

        // Act
        viewModel.resetCurrentGame()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.score.intValue).isEqualTo(0)
        assertThat(viewModel.moves.intValue).isEqualTo(0)
        assertThat(viewModel.gameWon.value).isFalse()
        assertThat(fakeTimerViewModel.elapsedSeconds.value).isEqualTo(0L)
        assertThat(fakeTimerViewModel.isTimerRunning()).isTrue() // Timer should restart
        assertThat(viewModel.playResetSound.value).isTrue()
    }

    @Test
    fun checkGamePlayCardTurnedWhenSameCardIsClickedTwiceFlipsItBackAndPenalizes() = runTest(testDispatcher) {
        // 1. Arrange
        advanceUntilIdle()
        val initialScore = viewModel.score.intValue
        val card = viewModel.tablePlay!!.cardsArray[0][0]
        val onSoundEvent = { _: SoundEvent -> } // We don't care about the sound here

        // Sanity check
        assertThat(card.value.turned).isFalse()

        // 2. Act
        // First click
        viewModel.checkGamePlayCardTurned(0, 0, onSoundEvent)
        // Check intermediate state
        assertThat(card.value.turned).isTrue()

        // Simulate time passing before the second click
        fakeTimerViewModel.setElapsedTime(2)

        // Second click on the same card
        viewModel.checkGamePlayCardTurned(0, 0, onSoundEvent)
        advanceUntilIdle()

        // 3. Assert
        assertThat(card.value.turned).isFalse() // Card should be flipped back
        assertThat(viewModel.moves.intValue).isEqualTo(2)
        assertThat(viewModel.score.intValue).isLessThan(initialScore) // Score should be penalized
    }

    @Test
    fun checkGamePlayCardTurnedWhenCoupledCardIsClickedDoesNothing() = runTest(testDispatcher) {
        // 1. Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        val (card1Pos, card2Pos) = findPairOnBoard(board.cardsArray)
        val onSoundEvent = { _: SoundEvent -> } // We don't care about the sound here

        // Couple the first pair
        viewModel.checkGamePlayCardTurned(card1Pos.first, card1Pos.second, onSoundEvent)
        viewModel.checkGamePlayCardTurned(card2Pos.first, card2Pos.second, onSoundEvent)
        advanceUntilIdle()

        // Pre-condition check
        assertThat(board.cardsArray[card1Pos.first][card1Pos.second].value.coupled).isTrue()

        val scoreAfterCouple = viewModel.score.intValue
        val movesAfterCouple = viewModel.moves.intValue

        // 2. Act
        // Click on the already coupled card
        viewModel.checkGamePlayCardTurned(card1Pos.first, card1Pos.second, onSoundEvent)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.moves.intValue).isEqualTo(movesAfterCouple)
        assertThat(viewModel.score.intValue).isEqualTo(scoreAfterCouple)
    }

    private fun findPairOnBoard(cards: Array<Array<MutableState<GameCard>>>): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val seenCards = mutableMapOf<Int, Pair<Int, Int>>()
        for (x in cards.indices) {
            for (y in cards[x].indices) {
                val card = cards[x][y].value
                if (seenCards.containsKey(card.id)) {
                    return Pair(seenCards[card.id]!!, Pair(x, y))
                }
                seenCards[card.id] = Pair(x, y)
            }
        }
        throw IllegalStateException("No pair found on the board for testing.")
    }

    private fun findNonPairOnBoard(cards: Array<Array<MutableState<GameCard>>>): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val firstCard = cards[0][0].value
        for (x in cards.indices) {
            for (y in cards[x].indices) {
                if (x == 0 && y == 0) continue
                val currentCard = cards[x][y].value
                if (currentCard.id != firstCard.id) {
                    return Pair(Pair(0, 0), Pair(x, y))
                }
            }
        }
        throw IllegalStateException("Could not find a non-matching pair for testing.")
    }

    private fun setupAlmostWonBoard(cards: Array<Array<MutableState<GameCard>>>): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val allCards = cards.flatMapIndexed { x, row ->
            row.mapIndexed { y, cardState -> Triple(x, y, cardState.value) }
        }
        val lastPairId = allCards.last().third.id
        val lastPairPositions = allCards.filter { it.third.id == lastPairId }

        // Mark all cards as coupled, except for the last pair
        cards.forEach { row ->
            row.forEach { cardState ->
                if (cardState.value.id != lastPairId) {
                    cardState.value = cardState.value.copy(coupled = true)
                }
            }
        }

        val card1Pos = Pair(lastPairPositions[0].first, lastPairPositions[0].second)
        val card2Pos = Pair(lastPairPositions[1].first, lastPairPositions[1].second)
        return Pair(card1Pos, card2Pos)
    }
}