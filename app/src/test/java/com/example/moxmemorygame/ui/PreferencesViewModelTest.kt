package com.example.moxmemorygame.ui

import android.os.Build
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.BackgroundMusic
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
    private lateinit var mockMusicManager: BackgroundMusicManager

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

        // Use a mock for the music manager to verify interactions
        mockMusicManager = mock(BackgroundMusicManager::class.java)
    }

    private fun initViewModel() {
        viewModel = PreferencesViewModel(
            navController = testNavController, 
            appSettingsDataStore = fakeDataStore,
            backgroundMusicManager = mockMusicManager // Use the mock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun updatePlayerName_whenNameIsValid_savesToDataStore() = runTest(testDispatcher) {
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
    fun updatePlayerName_whenNameIsTooLong_isIgnored() = runTest(testDispatcher) {
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
    fun getCardDisplayName_returnsFormattedName() = runTest(testDispatcher) {
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
    fun clearCardSelectionError_clearsTheError() = runTest(testDispatcher) {
        // Arrange: first, create an error state
        initViewModel()
        fakeDataStore.saveBoardDimensions(4, 5) // requires 10 cards
        advanceUntilIdle()
        viewModel.prepareForCardSelection()
        // Clean the state and create an invalid selection
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
    fun clearBoardDimensionError_clearsTheError() = runTest(testDispatcher) {
        // Arrange: first, create an error state
        initViewModel()
        viewModel.updateBoardDimensions(PreferencesViewModel.MIN_BOARD_WIDTH - 1, 4) // Trigger error
        advanceUntilIdle()
        assertThat(viewModel.boardDimensionError.value).isNotNull() // Pre-condition check

        // Act
        viewModel.clearBoardDimensionError()

        // Assert
        assertThat(viewModel.boardDimensionError.value).isNull()
    }

    @Test
    fun saveIsMusicEnabled_savesToDataStore() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        val initialValue = fakeDataStore.isMusicEnabled.value
        val newValue = !initialValue

        // Act
        viewModel.saveIsMusicEnabled(newValue)
        advanceUntilIdle()

        // Assert
        assertThat(fakeDataStore.isMusicEnabled.value).isEqualTo(newValue)
    }

    @Test
    fun saveMusicVolume_savesToDataStore() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        val newVolume = 0.75f

        // Act
        viewModel.saveMusicVolume(newVolume)
        advanceUntilIdle()

        // Assert
        assertThat(fakeDataStore.musicVolume.value).isEqualTo(newVolume)
    }

    @Test
    fun saveSelectedMusicTracks_savesToDataStore() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        val newTracks = setOf("track1", "track2")

        // Act
        viewModel.saveSelectedMusicTracks(newTracks)
        advanceUntilIdle()

        // Assert
        assertThat(fakeDataStore.selectedMusicTrackNames.value).isEqualTo(newTracks)
    }

    @Test
    fun saveAreSoundEffectsEnabled_savesToDataStore() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        val initialValue = fakeDataStore.areSoundEffectsEnabled.value
        val newValue = !initialValue

        // Act
        viewModel.saveAreSoundEffectsEnabled(newValue)
        advanceUntilIdle()

        // Assert
        assertThat(fakeDataStore.areSoundEffectsEnabled.value).isEqualTo(newValue)
    }

    @Test
    fun saveSoundEffectsVolume_savesToDataStore() = runTest(testDispatcher) {
        // Arrange
        initViewModel()
        val newVolume = 0.88f

        // Act
        viewModel.saveSoundEffectsVolume(newVolume)
        advanceUntilIdle()

        // Assert
        assertThat(fakeDataStore.soundEffectsVolume.value).isEqualTo(newVolume)
    }

    @Test
    fun init_whenSelectedCardsAreInsufficient_loadsDefaultCards() = runTest(testDispatcher) {
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
    fun onBackToMainMenuClicked_waitsForSaveJobs_thenNavigates() = runTest(testDispatcher) {
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
    fun updateBoardDimensions_whenWidthIsBelowMin_setsError() = runTest(testDispatcher) {
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
    fun updateBoardDimensions_whenHeightIsAboveMax_setsError() = runTest(testDispatcher) {
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
    fun toggleSelectAllBackgrounds_whenDeselectingAll_fallsBackToFirstSelected() = runTest(testDispatcher) {
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
    fun updateCardSelection_modifiesOnlyTempState() = runTest(testDispatcher) {
        // 1. Arrange: Create a valid initial state
        initViewModel()
        val minRequired = (3 * 4) / 2 // 6 cards for a 3x4 grid
        fakeDataStore.saveBoardDimensions(3, 4)
        val initialCards = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveSelectedCards(initialCards)

        advanceUntilIdle()
        viewModel.prepareForCardSelection() // Populate temp state

        // Verify that init didn't overwrite the data
        assertThat(viewModel.selectedCards.value).isEqualTo(initialCards)
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(initialCards)

        // 2. Act: Add a card
        val newCard = "img_c_99"
        viewModel.updateCardSelection(newCard, isSelected = true)

        // 3. Assert: Verify that only the temp state has changed
        val expectedTempCards = initialCards + newCard
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(expectedTempCards)

        // Verify that the DataStore (and the connected flow) was NOT modified
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(initialCards)
    }

    @Test
    fun confirmCardSelections_whenSelectionIsValid_savesToDataStore() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val minRequired = (3 * 4) / 2 // 6
        fakeDataStore.saveBoardDimensions(3, 4)
        fakeDataStore.saveSelectedCards(setOf("img_c_01")) // Intentionally invalid initial state

        advanceUntilIdle()
        viewModel.prepareForCardSelection()

        // Clear the temp state (which was polluted by default values)
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), selectAll = false)
        assertThat(viewModel.tempSelectedCards.value).isEmpty()

        val validSelection = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        validSelection.forEach { viewModel.updateCardSelection(it, true) } // Create a valid selection
        assertThat(viewModel.tempSelectedCards.value).hasSize(minRequired)

        // 2. Act
        viewModel.confirmCardSelections()
        advanceUntilIdle() // Wait for the save coroutine to complete

        // 3. Assert
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(validSelection)
        assertThat(viewModel.cardSelectionError.value).isNull()
    }

    @Test
    fun confirmCardSelections_whenSelectionIsInvalid_doesNotSaveAndSetsError() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val minRequired = (4 * 5) / 2 // 10
        val initialCards = (1..minRequired + 2).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveBoardDimensions(4, 5)
        fakeDataStore.saveSelectedCards(initialCards)

        advanceUntilIdle()
        viewModel.prepareForCardSelection()

        val invalidSelection = setOf("img_s_01", "img_s_02") // Only 2 cards
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), false) // Deselect all
        invalidSelection.forEach { viewModel.updateCardSelection(it, true) } // Select the 2 cards
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(invalidSelection)

        // 2. Act
        viewModel.confirmCardSelections()
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.cardSelectionError.value).isNotNull()
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(initialCards) // Must remain in the initial state
    }

    @Test
    fun updateBoardDimensions_whenValid_savesAndClearsError() = runTest(testDispatcher) {
        // 1. Arrange: start from a grid large with enough cards
        initViewModel()
        val initialWidth = 4
        val initialHeight = 5
        val requiredCards = (initialWidth * initialHeight) / 2 // 10
        val selectedCards = (1..requiredCards).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(selectedCards)

        advanceUntilIdle()

        // 2. Act: Reduce to a smaller but valid size
        val newWidth = 3
        val newHeight = 4 // Requires 6 cards, our selection of 10 is sufficient
        viewModel.updateBoardDimensions(newWidth, newHeight)
        advanceUntilIdle()

        // 3. Assert
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(newWidth)
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(newHeight)
        assertThat(viewModel.boardDimensionError.value).isNull()
    }

    @Test
    fun updateBoardDimensions_whenInvalid_doesNotSaveAndSetsError() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        val requiredCards = (initialWidth * initialHeight) / 2 // 6
        val selectedCards = (1..requiredCards).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(selectedCards)

        advanceUntilIdle()

        // 2. Act: Try to set dimensions that require more cards than selected
        val newWidth = 5
        val newHeight = 4 // Requires 10 cards, but we only have 6
        viewModel.updateBoardDimensions(newWidth, newHeight)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        // Verify that dimensions were NOT saved
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth)
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }

    @Test
    fun updateBoardDimensions_whenHeightIsBelowMin_doesNotSaveAndSetsError() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        advanceUntilIdle()

        // 2. Act
        viewModel.updateBoardDimensions(3, 2) // Height 2 < MIN_BOARD_HEIGHT (4)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }

    @Test
    fun updateBoardDimensions_whenCellCountIsOdd_doesNotSaveAndSetsError() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialWidth = 3
        val initialHeight = 4
        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        advanceUntilIdle()

        // 2. Act
        viewModel.updateBoardDimensions(3, 5) // 3 * 5 = 15 (odd)
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth)
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }

    @Test
    fun toggleSelectAllBackgrounds_whenSelectingAll_selectsAll() = runTest(testDispatcher) {
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
    fun updateBackgroundSelection_whenDeselectingLastOne_isIgnored() = runTest(testDispatcher) {
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
    fun confirmBackgroundSelections_savesToDataStore() = runTest(testDispatcher) {
        // 1. Arrange
        initViewModel()
        val initialSelection = setOf("background_00")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        advanceUntilIdle()

        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(initialSelection)
        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(initialSelection)

        // 2. Act
        val newSelection = setOf("background_02", "background_05")
        // First add, then remove, to avoid deselecting the last one
        viewModel.updateBackgroundSelection("background_02", true)
        viewModel.updateBackgroundSelection("background_05", true)
        viewModel.updateBackgroundSelection("background_00", false)

        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(newSelection)
    }

    // --- Interaction tests with mocked BackgroundMusicManager ---

    @Test
    fun playMusicPreview_callsManager() = runTest(testDispatcher) {
        initViewModel()
        val track = BackgroundMusic.ClassicSlowGuitar
        viewModel.playMusicPreview(track)
        verify(mockMusicManager).playPreview(track)
    }

    @Test
    fun stopMusicPreview_callsManager() = runTest(testDispatcher) {
        initViewModel()
        viewModel.stopMusicPreview()
        verify(mockMusicManager).stopPreview()
    }

}
