package com.example.moxmemorygame.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import com.example.moxmemorygame.R
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.ui.BackgroundMusicManager
import com.example.moxmemorygame.ui.PreferencesViewModel
import com.example.moxmemorygame.ui.Screen
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PreferencesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeDataStore: FakeAppSettingsDataStore
    private lateinit var navController: TestNavHostController
    private lateinit var fakeMusicManager: BackgroundMusicManager

    @Before
    fun setup() {
        fakeDataStore = FakeAppSettingsDataStore()
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        fakeMusicManager = BackgroundMusicManager(
            context = ApplicationProvider.getApplicationContext(),
            appSettingsDataStore = fakeDataStore,
            externalScope = CoroutineScope(Dispatchers.IO)
        )
    }

    private fun createViewModel() = PreferencesViewModel(
        navController = navController,
        appSettingsDataStore = fakeDataStore,
        backgroundMusicManager = fakeMusicManager
    )

    @Test
    fun backButton_navigatesToOpeningMenu() = runTest {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(
                navController = navController,
                // To test popping the back stack, we need a back stack. 
                // Start at the screen we expect to navigate *back* to.
                startDestination = Screen.OpeningMenuScreen.route
            ) {
                composable(Screen.OpeningMenuScreen.route) {
                    Text("Opening Menu Screen")
                }
                composable(Screen.PreferencesScreen.route) {
                    PreferencesScreen(
                        preferencesViewModel = viewModel,
                        innerPadding = PaddingValues(0.dp)
                    )
                }
            }
        }

        // Navigate to the screen we are testing
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.PreferencesScreen.route)
        }
        composeTestRule.waitForIdle()

        val backButtonText = ApplicationProvider.getApplicationContext<android.content.Context>().getString(R.string.preferences_button_back_to_main_menu)
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(backButtonText, ignoreCase = true))
        composeTestRule.onNodeWithText(backButtonText, ignoreCase = true).performClick()

        composeTestRule.waitForIdle()

        // Now, we expect to be back at the opening menu screen
        composeTestRule.onNodeWithText("Opening Menu Screen").assertIsDisplayed()
    }

    @Test
    fun playerName_canBeUpdated() = runTest {
        val newName = "Mox"
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val saveButtonText = context.getString(R.string.preferences_button_save_player_name)

        val viewModel = createViewModel()

        composeTestRule.setContent {
            PreferencesScreen(
                preferencesViewModel = viewModel,
                innerPadding = PaddingValues(0.dp)
            )
        }
        composeTestRule.waitForIdle()

        val initialName = viewModel.playerName.value

        composeTestRule.onNodeWithText(initialName).performTextClearance()
        composeTestRule.onNodeWithText("").performTextInput(newName)
        composeTestRule.onNodeWithText(saveButtonText, ignoreCase = true).performClick()

        assertThat(fakeDataStore.playerName.value).isEqualTo(newName)
    }

    @Test
    fun backgroundSelection_canBeUpdated() = runTest {
        val initialSelection = setOf("background_01", "background_03")
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        val viewModel = createViewModel()

        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val selectButtonText = context.getString(R.string.preferences_button_select_backgrounds, initialSelection.size)
        val dialogTitle = context.getString(R.string.background_selection_dialog_title)
        val okButtonText = context.getString(R.string.button_ok)

        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(selectButtonText, ignoreCase = true))
        composeTestRule.onNodeWithText(selectButtonText, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("Background 01").performClick() // Deselect
        composeTestRule.onNodeWithText("Background 02").performClick() // Select
        composeTestRule.onNodeWithText(okButtonText, ignoreCase = true).performClick()

        viewModel.confirmBackgroundSelections()
        composeTestRule.waitForIdle()

        val expectedSelection = setOf("background_03", "background_02")
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(expectedSelection)
    }

    @Test
    fun boardDimensions_showsErrorWhenNotEnoughCards_width() = runTest {
        val initialWidth = 3
        val initialHeight = 4
        val newWidth = PreferencesViewModel.MAX_BOARD_WIDTH // swipeRight is a maximal gesture
        val requiredPairs = (newWidth * initialHeight) / 2
        val initialCards = (1..6).map { "img_c_%02d".format(it) }.toSet()
        val errorText = "Board size ${newWidth}x${initialHeight} requires $requiredPairs pairs. You have ${initialCards.size}."

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag("WidthSlider"))
        composeTestRule.onNodeWithTag("WidthSlider").performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth)
    }

    @Test
    fun boardDimensions_showsErrorWhenNotEnoughCards_height() = runTest {
        val initialWidth = 3
        val initialHeight = 4
        val newHeight = PreferencesViewModel.MAX_BOARD_HEIGHT // swipeRight is a maximal gesture
        val requiredPairs = (initialWidth * newHeight) / 2
        val initialCards = (1..6).map { "img_c_%02d".format(it) }.toSet()
        val errorText = "Board size ${initialWidth}x$newHeight requires $requiredPairs pairs. You have ${initialCards.size}."

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag("HeightSlider"))
        composeTestRule.onNodeWithTag("HeightSlider").performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
        assertThat(fakeDataStore.selectedBoardHeight.value).isEqualTo(initialHeight)
    }
    
    @Test
    fun boardDimensions_canBeUpdatedWhenEnoughCards() = runTest {
        val initialWidth = 3
        val initialHeight = 4
        val newWidth = 4
        val requiredPairs = (newWidth * initialHeight) / 2 // 8
        // Ensure we have more than enough cards
        val initialCards = (1..(requiredPairs + 5)).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        // Change width
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag("WidthSlider"))
        composeTestRule.onNodeWithTag("WidthSlider").performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()

        // Assert that the new width is saved and no error is shown
        assertThat(fakeDataStore.selectedBoardWidth.value).isGreaterThan(initialWidth)
        // We can't easily get the error text if it doesn't exist, so we check the ViewModel state
        assertThat(viewModel.boardDimensionError.value).isNull()
    }

    @Test
    fun simpleCardSelection_canBeUpdatedCorrectly() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialWidth = 2
        val initialHeight = 2
        val initialCards = (1..2).map { "img_s_%02d".format(it) }.toSet()
        val totalSimpleCards = 10

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val simpleCardsButtonTag = "SimpleCardsButton"
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag(simpleCardsButtonTag))
        composeTestRule.onNodeWithTag(simpleCardsButtonTag).performClick()

        composeTestRule.onNodeWithText("Simple 1").performClick() // Deselect
        val newDialogTitle = context.getString(R.string.preferences_button_select_simple_cards, 1, totalSimpleCards)
        composeTestRule.onNodeWithText(newDialogTitle, ignoreCase = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("Simple 3").performClick() // Select
        composeTestRule.onNodeWithText(context.getString(R.string.button_ok), ignoreCase = true).performClick()

        viewModel.confirmCardSelections()
        composeTestRule.waitForIdle()

        val expectedCards = initialCards.toMutableSet().apply {
            remove("img_s_01")
            add("img_s_03")
        }.toSet()
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(expectedCards)
    }

    @Test
    fun music_canBeToggledAndVolumeChanged() = runTest {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val preferencesListTag = "PreferencesList"
        val musicSwitchTag = "MusicSwitch"
        val musicSliderTag = "MusicVolumeSlider"

        // 1. Initially, Music is enabled
        val initialVolume = fakeDataStore.musicVolume.value
        assertThat(fakeDataStore.isMusicEnabled.value).isTrue()

        // 2. Disable music
        composeTestRule.onNodeWithTag(preferencesListTag).performScrollToNode(hasTestTag(musicSwitchTag))
        composeTestRule.onNodeWithTag(musicSwitchTag).performClick()
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.isMusicEnabled.value).isFalse()

        // The slider should be disabled now
        composeTestRule.onNodeWithTag(preferencesListTag).performScrollToNode(hasTestTag(musicSliderTag))
        composeTestRule.onNodeWithTag(musicSliderTag).assertIsNotEnabled()

        // 3. Re-enable music
        composeTestRule.onNodeWithTag(preferencesListTag).performScrollToNode(hasTestTag(musicSwitchTag))
        composeTestRule.onNodeWithTag(musicSwitchTag).performClick()
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.isMusicEnabled.value).isTrue()

        // The slider should be enabled again
        composeTestRule.onNodeWithTag(preferencesListTag).performScrollToNode(hasTestTag(musicSliderTag))
        composeTestRule.onNodeWithTag(musicSliderTag).assertIsEnabled()

        // 4. Change volume
        composeTestRule.onNodeWithTag(musicSliderTag).performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.musicVolume.value).isGreaterThan(initialVolume)
    }

    @Test
    fun cardSelection_disablesConfirmWhenNotEnoughCards() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialWidth = 3
        val initialHeight = 4
        val minRequired = (initialWidth * initialHeight) / 2 // 6
        val initialCards = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val refinedCardsButtonTag = "RefinedCardsButton"
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag(refinedCardsButtonTag))
        composeTestRule.onNodeWithTag(refinedCardsButtonTag).performClick()

        val okButtonNode = composeTestRule.onNodeWithText(context.getString(R.string.button_ok), ignoreCase = true)
        okButtonNode.assertIsEnabled()

        composeTestRule.onNodeWithText("Refined 1").performClick() // Deselect, now has 5 cards
        composeTestRule.waitForIdle()

        okButtonNode.assertIsNotEnabled()
    }

    @Test
    fun cardSelection_canBeUpdatedCorrectly() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialWidth = 3
        val initialHeight = 4
        val initialCards = (1..6).map { "img_c_%02d".format(it) }.toSet()
        val totalRefinedCards = 20

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val refinedCardsButtonTag = "RefinedCardsButton"
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag(refinedCardsButtonTag))
        composeTestRule.onNodeWithTag(refinedCardsButtonTag).performClick()

        composeTestRule.onNodeWithText("Refined 1").performClick() // Deselect
        val newDialogTitle = context.getString(R.string.preferences_button_select_refined_cards, 5, totalRefinedCards)
        composeTestRule.onNodeWithText(newDialogTitle, ignoreCase = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("Refined 7").performClick() // Select
        composeTestRule.onNodeWithText(context.getString(R.string.button_ok), ignoreCase = true).performClick()

        viewModel.confirmCardSelections()
        composeTestRule.waitForIdle()

        val expectedCards = initialCards.toMutableSet().apply {
            remove("img_c_01")
            add("img_c_07")
        }.toSet()
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(expectedCards)
    }

    @Test
    fun musicSelectionDialog_canSelectNone() = runTest {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialCount = viewModel.selectedMusicTrackNames.value.size
        val selectMusicTracksText = context.getString(R.string.preferences_button_select_music_tracks, initialCount)
        val noneButtonText = context.getString(R.string.preferences_music_selection_none)

        // Open the dialog and click the "None" button
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(selectMusicTracksText, ignoreCase = true))
        composeTestRule.onNodeWithText(selectMusicTracksText, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText(noneButtonText, ignoreCase = true).performClick()
        composeTestRule.waitForIdle()

        // Assert that the selection is now empty and the dialog is gone
        assertThat(fakeDataStore.selectedMusicTrackNames.value).isEmpty()
        composeTestRule.onNodeWithText(context.getString(R.string.preferences_music_selection_dialog_title)).assertDoesNotExist()
    }

    @Test
    fun soundEffects_canBeToggledAndVolumeChanged() = runTest {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val preferencesListTag = "PreferencesList"
        val sfxSwitchTag = "SfxSwitch"
        val sfxSliderTag = "SfxVolumeSlider"
        val listNode = composeTestRule.onNodeWithTag(preferencesListTag)

        // 1. Initially, SFX are enabled
        val initialVolume = fakeDataStore.soundEffectsVolume.value
        assertThat(fakeDataStore.areSoundEffectsEnabled.value).isTrue()

        // 2. Disable sound effects
        listNode.performScrollToNode(hasTestTag(sfxSwitchTag))
        composeTestRule.onNodeWithTag(sfxSwitchTag).performClick()
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.areSoundEffectsEnabled.value).isFalse()

        // The slider should be disabled now
        listNode.performScrollToNode(hasTestTag(sfxSliderTag))
        composeTestRule.onNodeWithTag(sfxSliderTag).assertIsNotEnabled()

        // 3. Re-enable sound effects
        listNode.performScrollToNode(hasTestTag(sfxSwitchTag))
        composeTestRule.onNodeWithTag(sfxSwitchTag).performClick()
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.areSoundEffectsEnabled.value).isTrue()

        // The slider should be enabled again
        listNode.performScrollToNode(hasTestTag(sfxSliderTag))
        composeTestRule.onNodeWithTag(sfxSliderTag).assertIsEnabled()

        // 4. Change volume
        listNode.performScrollToNode(hasTestTag(sfxSliderTag))
        composeTestRule.onNodeWithTag(sfxSliderTag).performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.soundEffectsVolume.value).isGreaterThan(initialVolume)
    }

    @Test
    fun backgroundSelection_selectAll_worksAsExpected() = runTest {
        val initialSelection = setOf("background_00") // Start with one
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        val viewModel = createViewModel()
        val allBackgrounds = viewModel.availableBackgrounds.toSet()

        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val selectButtonText = context.getString(R.string.preferences_button_select_backgrounds, initialSelection.size)
        val selectAllButtonText = context.getString(R.string.dialog_select_deselect_all)
        val okButtonText = context.getString(R.string.button_ok)

        // Open the dialog
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(selectButtonText, ignoreCase = true))
        composeTestRule.onNodeWithText(selectButtonText, ignoreCase = true).performClick()

        // Click "Select All"
        composeTestRule.onNodeWithText(selectAllButtonText).performClick()
        composeTestRule.waitForIdle()

        // Confirm and close dialog
        composeTestRule.onNodeWithText(okButtonText).performClick()
        viewModel.confirmBackgroundSelections() // Manually trigger confirmation
        composeTestRule.waitForIdle()

        // Assert that all backgrounds are now saved in the data store
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(allBackgrounds)
    }
    @Test
    fun backgroundSelection_isNotSavedOnBackPress() = runTest {
        val initialSelection = setOf("background_01")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        val viewModel = createViewModel()

        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val selectButtonText = context.getString(R.string.preferences_button_select_backgrounds, initialSelection.size)
        val dialogTitle = context.getString(R.string.background_selection_dialog_title)

        // Open the dialog
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(selectButtonText, ignoreCase = true))
        composeTestRule.onNodeWithText(selectButtonText, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()

        // Make a change
        composeTestRule.onNodeWithText("Background 02").performClick()

        // Simulate a back press
        Espresso.pressBack()
        composeTestRule.waitForIdle()

        // Assert the dialog is gone
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()

        // Assert that the data store still holds the initial selection
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(initialSelection)
    }
}