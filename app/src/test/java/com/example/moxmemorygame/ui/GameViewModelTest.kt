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
@Config(sdk = [Build.VERSION_CODES.Q]) // Specifica l'SDK da simulare
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
    fun `initial state is correct`() = runTest(testDispatcher) {
        initViewModel()
        advanceUntilIdle()
        
        assertThat(viewModel.isBoardInitialized.value).isTrue()
        assertThat(viewModel.moves.intValue).isEqualTo(0)
        assertThat(viewModel.score.intValue).isEqualTo(0)
        assertThat(viewModel.tablePlay).isNotNull()
        assertThat(viewModel.tablePlay?.cardsArray?.all { row -> row.all { it.value != null } }).isTrue()
    }

    @Test
    fun `loading uses default cards when user selection is insufficient`() = runTest(testDispatcher) {
        // Arrange
        val boardWidth = 4
        val boardHeight = 6
        val requiredCards = (boardWidth * boardHeight) / 2
        val insufficientCards = setOf("img_c_01", "img_c_02")
        
        fakeDataStore.saveBoardDimensions(boardWidth, boardHeight)
        fakeDataStore.saveSelectedCards(insufficientCards)
        
        // Act: ViewModel initialization triggers the card loading logic
        initViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.gameCardImages.size).isEqualTo(requiredCards)
    }

    @Test
    fun `requestResetDialog sets correct states`() {
        initViewModel()
        // Act
        viewModel.requestResetDialog()

        // Assert
        assertThat(viewModel.gamePaused.value).isTrue()
        assertThat(viewModel.gameResetRequest.value).isTrue()
    }

    @Test
    fun `cancelResetDialog correctly cancels the reset request`() {
        initViewModel()
        // Arrange
        viewModel.requestResetDialog()

        // Act
        viewModel.cancelResetDialog()

        // Assert
        assertThat(viewModel.gamePaused.value).isFalse()
        assertThat(viewModel.gameResetRequest.value).isFalse()
    }

    @Test
    fun `navigateToOpeningMenuAndCleanupDialogStates cleans states and navigates`() {
        initViewModel()
        // Arrange
        viewModel.gameWon.value = true
        viewModel.gamePaused.value = true
        viewModel.gameResetRequest.value = true

        // Act
        viewModel.navigateToOpeningMenuAndCleanupDialogStates()

        // Assert
        assertThat(viewModel.gameWon.value).isFalse()
        assertThat(viewModel.gamePaused.value).isFalse()
        assertThat(viewModel.gameResetRequest.value).isFalse()
        assertThat(testNavController.currentDestination?.route).isEqualTo(Screen.OpeningMenuScreen.route)
    }

    @Test
    fun `checkGamePlayCardTurned when first card is turned updates state correctly`() = runTest(testDispatcher) {
        initViewModel()
        // 1. Arrange
        advanceUntilIdle()
        
        var flipSoundCalled = false
        val flipSound = { flipSoundCalled = true }

        val cardBeforeTurn = viewModel.tablePlay!!.cardsArray[0][0].value
        assertThat(cardBeforeTurn.turned).isFalse()

        // 2. Act
        viewModel.checkGamePlayCardTurned(
            x = 0, y = 0, flipSound = flipSound, pauseSound = {}, failSound = {}, successSound = {}, winSound = {}
        )

        // 3. Assert
        val cardAfterTurn = viewModel.tablePlay!!.cardsArray[0][0].value
        assertThat(cardAfterTurn.turned).isTrue()
        assertThat(viewModel.moves.intValue).isEqualTo(1)
        assertThat(flipSoundCalled).isTrue()
    }

    @Test
    fun `checkGamePlayCardTurned when correct pair is turned updates score and state`() = runTest(testDispatcher) {
        initViewModel()
        // 1. Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        val (card1Pos, card2Pos) = findPairOnBoard(board.cardsArray)
        val initialScore = viewModel.score.intValue

        var successSoundCalled = false
        val successSound = { successSoundCalled = true }

        // 2. Act
        viewModel.checkGamePlayCardTurned(card1Pos.first, card1Pos.second, {}, {}, {}, {}, {})
        fakeTimerViewModel.setElapsedTime(2) // Simula 2 secondi passati
        viewModel.checkGamePlayCardTurned(card2Pos.first, card2Pos.second, {}, {}, {}, successSound, {})
        advanceUntilIdle()

        // 3. Assert
        val card1 = board.cardsArray[card1Pos.first][card1Pos.second].value
        val card2 = board.cardsArray[card2Pos.first][card2Pos.second].value

        assertThat(card1.coupled).isTrue()
        assertThat(card2.coupled).isTrue()
        assertThat(viewModel.score.intValue).isGreaterThan(initialScore)
        assertThat(viewModel.moves.intValue).isEqualTo(2)
        assertThat(successSoundCalled).isTrue()
    }

    @Test
    fun `checkGamePlayCardTurned when incorrect pair is turned flips them back`() = runTest(testDispatcher) {
        initViewModel()
        // 1. Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        val (card1Pos, card2Pos) = findNonPairOnBoard(board.cardsArray)

        var failSoundCalled = false
        val failSound = { failSoundCalled = true }

        // 2. Act
        viewModel.checkGamePlayCardTurned(card1Pos.first, card1Pos.second, {}, {}, {}, {}, {})
        fakeTimerViewModel.setElapsedTime(2) // Simula 2 secondi passati
        viewModel.checkGamePlayCardTurned(card2Pos.first, card2Pos.second, {}, {}, failSound, {}, {})
        
        advanceUntilIdle()

        // 3. Assert
        val card1 = board.cardsArray[card1Pos.first][card1Pos.second].value
        val card2 = board.cardsArray[card2Pos.first][card2Pos.second].value

        assertThat(card1.turned).isFalse()
        assertThat(card2.turned).isFalse()
        assertThat(failSoundCalled).isTrue()
    }

    @Test
    fun `checkGamePlayCardTurned when last pair is found triggers win condition`() = runTest(testDispatcher) {
        initViewModel()
        // 1. Arrange
        advanceUntilIdle()
        val board = viewModel.tablePlay!!
        // Simula una partita quasi vinta, lasciando solo una coppia da scoprire
        val (lastPairCard1, lastPairCard2) = setupAlmostWonBoard(board.cardsArray)
        viewModel.score.intValue = 900 // Simula un punteggio esistente

        var winSoundCalled = false
        val winSound = { winSoundCalled = true }

        // 2. Act
        // Gira la prima carta dell'ultima coppia
        viewModel.checkGamePlayCardTurned(lastPairCard1.first, lastPairCard1.second, {}, {}, {}, {}, {})
        fakeTimerViewModel.setElapsedTime(2) // Simula 2 secondi passati
        // Gira la seconda e scatena la vittoria
        viewModel.checkGamePlayCardTurned(lastPairCard2.first, lastPairCard2.second, {}, {}, {}, {}, winSound)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.gameWon.value).isTrue()
        assertThat(fakeTimerViewModel.isTimerRunning()).isFalse()
        assertThat(winSoundCalled).isTrue()
        assertThat(viewModel.gamePaused.value).isTrue() // Verifica che il dialogo di pausa/vittoria sia richiesto

        // Controlla che il punteggio finale sia stato salvato
        val finalScore = viewModel.score.intValue
        assertThat(fakeDataStore.lastPlayedEntry.value?.score).isEqualTo(finalScore)
    }

    @Test
    fun `resetCurrentGame resets all game state`() = runTest(testDispatcher) {
        initViewModel()
        // 1. Arrange: Simulate a game in progress
        advanceUntilIdle()
        viewModel.score.intValue = 500
        viewModel.moves.intValue = 10
        fakeTimerViewModel.setElapsedTime(60) // 1 minute passed
        viewModel.gameWon.value = true // Simulate a won state to ensure it gets reset

        // 2. Act
        viewModel.resetCurrentGame()
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.score.intValue).isEqualTo(0)
        assertThat(viewModel.moves.intValue).isEqualTo(0)
        assertThat(viewModel.gameWon.value).isFalse()
        assertThat(fakeTimerViewModel.elapsedSeconds.value).isEqualTo(0L)
        assertThat(fakeTimerViewModel.isTimerRunning()).isTrue() // Timer should restart
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

        // Marca tutte le carte come accoppiate, tranne l'ultima coppia
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