package com.example.moxmemorygame.ui

import android.os.Build
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class PreferencesViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var viewModel: PreferencesViewModel
    private lateinit var fakeDataStore: FakeAppSettingsDataStore
    private lateinit var testNavController: TestNavHostController

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        fakeDataStore = FakeAppSettingsDataStore()
        testNavController = TestNavHostController(ApplicationProvider.getApplicationContext())
        testNavController.navigatorProvider.addNavigator(ComposeNavigator())
        testNavController.graph = testNavController.createGraph(startDestination = "preferences") {
            composable("preferences") { }
            composable(Screen.OpeningMenuScreen.route) { }
        }
    }

    private fun initViewModel() {
        viewModel = PreferencesViewModel(navController = testNavController, appSettingsDataStore = fakeDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `init when selected cards are insufficient loads default cards`() = runTest(testDispatcher) {
        // Arrange
        val boardWidth = 4
        val boardHeight = 6 // Requires 12 cards
        val insufficientCards = setOf("img_c_01", "img_c_02") // Only 2 cards

        fakeDataStore.saveBoardDimensions(boardWidth, boardHeight)
        fakeDataStore.saveSelectedCards(insufficientCards)

        // Act
        initViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.selectedCards.value).isEqualTo(IAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(IAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
    }

    @Test
    fun `onBackToMainMenuClicked waits for save jobs then navigates`() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        fakeDataStore.setSaveDelay(2000) // Mock a long save operation
        testNavController.setCurrentDestination("preferences")

        // Act
        viewModel.confirmBackgroundSelections() // This will start a long-running save job
        viewModel.onBackToMainMenuClicked()

        // Assert
        // Immediately after calling, we should still be on the same screen because the save is in progress
        assertThat(testNavController.currentDestination?.route).isEqualTo("preferences")

        // Advance time to allow the save to complete
        advanceUntilIdle()

        // Now, we expect navigation to have occurred
        assertThat(testNavController.currentDestination?.route).isNotEqualTo("preferences")
    }

    @Test
    fun `updateBoardDimensions when width is below min sets error`() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        advanceUntilIdle()
        val invalidWidth = PreferencesViewModel.MIN_BOARD_WIDTH - 1

        // Act
        viewModel.updateBoardDimensions(invalidWidth, PreferencesViewModel.MIN_BOARD_HEIGHT)
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
    }

    @Test
    fun `updateBoardDimensions when height is above max sets error`() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        advanceUntilIdle()
        val invalidHeight = PreferencesViewModel.MAX_BOARD_HEIGHT + 1

        // Act
        viewModel.updateBoardDimensions(PreferencesViewModel.MIN_BOARD_WIDTH, invalidHeight)
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
    }


    @Test
    fun `toggleSelectAllBackgrounds when deselecting all falls back to first selected`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialSelection = setOf("background_02", "background_04", "background_00")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        advanceUntilIdle()

        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(initialSelection)

        // 2. Act
        viewModel.prepareForBackgroundSelection()
        viewModel.toggleSelectAllBackgrounds(selectAll = false)

        // 3. Assert
        val expectedFallback = "background_00"
        assertThat(viewModel.selectedBackgrounds.value).containsExactly(expectedFallback)
    }

    @Test
    fun `updateCardSelection modifies only temp state`() = runTest(testDispatcher) {
        // 1. Arrange: Crea uno stato iniziale VALIDO
        initViewModel()
        val minRequired = (3 * 4) / 2 // 6 carte per una griglia 3x4
        fakeDataStore.saveBoardDimensions(3, 4)
        val initialCards = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveSelectedCards(initialCards)

        advanceUntilIdle()
        viewModel.prepareForCardSelection() // Popola lo stato temporaneo

        // Verifica che l'init non abbia sovrascritto i dati
        assertThat(viewModel.selectedCards.value).isEqualTo(initialCards)
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(initialCards)
        
        // 2. Act: Aggiungi una carta
        val newCard = "img_c_99"
        viewModel.updateCardSelection(newCard, isSelected = true)

        // 3. Assert: Verifica che solo lo stato temporaneo sia cambiato
        val expectedTempCards = initialCards + newCard
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(expectedTempCards)
        
        // Verifica che il DataStore (e il flow collegato) NON sia stato modificato
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(initialCards)
    }

    @Test
    fun `confirmCardSelections when selection is valid saves to DataStore`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val minRequired = (3 * 4) / 2 // 6
        fakeDataStore.saveBoardDimensions(3, 4)
        fakeDataStore.saveSelectedCards(setOf("img_c_01")) // Stato iniziale volutamente non valido

        advanceUntilIdle()
        viewModel.prepareForCardSelection()

        // Pulisci lo stato temporaneo (che era stato inquinato dai valori di default)
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), selectAll = false)
        assertThat(viewModel.tempSelectedCards.value).isEmpty()

        val validSelection = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        validSelection.forEach { viewModel.updateCardSelection(it, true) } // Crea una selezione valida
        assertThat(viewModel.tempSelectedCards.value).hasSize(minRequired)

        // 2. Act
        viewModel.confirmCardSelections()
        advanceUntilIdle() // Attendi il completamento della coroutine di salvataggio

        // 3. Assert
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(validSelection)
        assertThat(viewModel.cardSelectionError.value).isNull()
    }

    @Test
    fun `confirmCardSelections when selection is invalid does not save and sets error`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val minRequired = (4 * 5) / 2 // 10
        val initialCards = (1..minRequired + 2).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveBoardDimensions(4, 5)
        fakeDataStore.saveSelectedCards(initialCards)

        advanceUntilIdle()
        viewModel.prepareForCardSelection()

        val invalidSelection = setOf("img_s_01", "img_s_02") // Solo 2 carte
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), false) // Deseleziona tutto
        invalidSelection.forEach { viewModel.updateCardSelection(it, true) } // Seleziona le 2 carte
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(invalidSelection)

        // 2. Act
        viewModel.confirmCardSelections()
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.cardSelectionError.value).isNotNull()
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(initialCards) // Deve rimanere lo stato iniziale
    }

    @Test
    fun `updateBoardDimensions when valid saves and clears error`() = runTest(testDispatcher) {
        // 1. Arrange: Partiamo da una griglia grande con abbastanza carte
        initViewModel()
        val initialWidth = 4
        val initialHeight = 5
        val requiredCards = (initialWidth * initialHeight) / 2 // 10
        val selectedCards = (1..requiredCards).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(selectedCards)

        advanceUntilIdle()

        // 2. Act: Riduciamo a una dimensione più piccola ma valida
        val newWidth = 3
        val newHeight = 4 // Richiede 6 carte, la nostra selezione di 10 è sufficiente
        viewModel.updateBoardDimensions(newWidth, newHeight)
        advanceUntilIdle()

        // 3. Assert
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(newWidth)
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(newHeight)
        assertThat(viewModel.boardDimensionError.value).isNull()
    }

    @Test
    fun `updateBoardDimensions when invalid does not save and sets error`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        val requiredCards = (initialWidth * initialHeight) / 2 // 6
        val selectedCards = (1..requiredCards).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(selectedCards)

        advanceUntilIdle()

        // 2. Act: Prova a impostare dimensioni che richiedono più carte di quelle selezionate
        val newWidth = 5
        val newHeight = 4 // Richiede 10 carte, ma ne abbiamo solo 6
        viewModel.updateBoardDimensions(newWidth, newHeight)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        // Verifica che le dimensioni NON siano state salvate
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth)
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }

    @Test
    fun `updateBoardDimensions when height is below min does not save and sets error`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        advanceUntilIdle()

        // 2. Act
        viewModel.updateBoardDimensions(3, 2) // Altezza 2 < MIN_BOARD_HEIGHT (4)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }

    @Test
    fun `updateBoardDimensions when cell count is odd does not save and sets error`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        advanceUntilIdle()

        // 2. Act
        viewModel.updateBoardDimensions(3, 5) // 3 * 5 = 15 (dispari)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth)
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }

    @Test
    fun `toggleSelectAllBackgrounds when selecting all selects all`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialSelection = setOf("background_00")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        advanceUntilIdle()

        // 2. Act
        viewModel.toggleSelectAllBackgrounds(selectAll = true)

        // 3. Assert
        val allBackgrounds = viewModel.availableBackgrounds.toSet()
        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(allBackgrounds)
    }

    @Test
    fun `updateBackgroundSelection when deselecting last one is ignored`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialSelection = setOf("background_01")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        advanceUntilIdle()

        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(initialSelection)

        // 2. Act
        viewModel.updateBackgroundSelection("background_01", isSelected = false)

        // 3. Assert
        // The selection should not have changed because it was the last one
        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(initialSelection)
    }

    @Test
    fun `confirmBackgroundSelections saves to DataStore`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialSelection = setOf("background_00")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        advanceUntilIdle()

        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(initialSelection)
        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(initialSelection)

        // 2. Act
        val newSelection = setOf("background_02", "background_05")
        // Prima aggiungi, poi rimuovi, per evitare di deselezionare l'ultimo elemento
        viewModel.updateBackgroundSelection("background_02", true)
        viewModel.updateBackgroundSelection("background_05", true)
        viewModel.updateBackgroundSelection("background_00", false) 

        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(newSelection)
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(initialSelection)

        viewModel.confirmBackgroundSelections()
        advanceUntilIdle()

        // 3. Assert
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(newSelection)
    }

    @Test
    fun `updatePlayerName when name is valid saves to DataStore`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialName = "Player1"
        fakeDataStore.savePlayerName(initialName)
        advanceUntilIdle()

        // 2. Act
        val newName = "NewPlayer"
        viewModel.updatePlayerName(newName)
        advanceUntilIdle()

        // 3. Assert
        assertThat(fakeDataStore.playerName.value).isEqualTo(newName)
    }

    @Test
    fun `updatePlayerName when name is too long is ignored`() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialName = "Player1"
        fakeDataStore.savePlayerName(initialName)
        advanceUntilIdle()

        // 2. Act
        val longName = "a".repeat(PreferencesViewModel.PLAYERNAME_MAX_LENGTH + 1)
        viewModel.updatePlayerName(longName)
        advanceUntilIdle()

        // 3. Assert
        assertThat(fakeDataStore.playerName.value).isEqualTo(initialName)
    }
    
    @Test
    fun `getCardDisplayName returns formatted name`() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        
        // Act & Assert
        val refinedName = viewModel.getCardDisplayName("img_c_05")
        assertThat(refinedName).isEqualTo("Refined 5")

        val simpleName = viewModel.getCardDisplayName("img_s_08")
        assertThat(simpleName).isEqualTo("Simple 8")

        val unknownName = viewModel.getCardDisplayName("unknown_resource")
        assertThat(unknownName).isEqualTo("unknown_resource")
    }

    @Test
    fun `clearCardSelectionError clears the error`() = runTest(testDispatcher) {
        // Arrange: first, create an error state
        initViewModel()
        fakeDataStore.saveBoardDimensions(4, 5) // requires 10 cards
        advanceUntilIdle()
        viewModel.prepareForCardSelection()
        // Pulisci lo stato e crea una selezione invalida
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), false)
        viewModel.updateCardSelection("img_c_01", true) // select only 1 card
        viewModel.confirmCardSelections()
        assertThat(viewModel.cardSelectionError.value).isNotNull()

        // Act
        viewModel.clearCardSelectionError()

        // Assert
        assertThat(viewModel.cardSelectionError.value).isNull()
    }

    @Test
    fun `clearBoardDimensionError clears the error`() = runTest(testDispatcher) {
        // Arrange: first, create an error state
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        val cardsForInitialBoard = (initialWidth * initialHeight) / 2 // 6
        val selectedCards = (1..cardsForInitialBoard).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(selectedCards)

        advanceUntilIdle()
        viewModel.updateBoardDimensions(5, 4) // This action will cause an error (requires 10 cards, we have 6)
        assertThat(viewModel.boardDimensionError.value).isNotNull()

        // Act
        viewModel.clearBoardDimensionError()

        // Assert
        assertThat(viewModel.boardDimensionError.value).isNull()
    }
}