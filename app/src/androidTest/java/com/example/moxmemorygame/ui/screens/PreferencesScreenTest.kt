package com.example.moxmemorygame.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.moxmemorygame.R
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.ui.PreferencesViewModel
import com.google.common.truth.Truth.assertThat
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

    @Before
    fun setup() {
        // Prepare objects that are identical for every test.
        // The ViewModel is now created inside each test to ensure a clean state.
        fakeDataStore = FakeAppSettingsDataStore()
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun playerName_canBeUpdated() = runTest {
        val newName = "Mox"
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val saveButtonText = context.getString(R.string.preferences_button_save_player_name)

        // 1. Create the ViewModel for this specific test.
        val viewModel = PreferencesViewModel(navController = navController, appSettingsDataStore = fakeDataStore)

        // 2. Set content and wait for the UI to be idle.
        composeTestRule.setContent {
            PreferencesScreen(
                preferencesViewModel = viewModel,
                innerPadding = PaddingValues(0.dp)
            )
        }
        composeTestRule.waitForIdle()

        val initialName = viewModel.playerName.value

        // 3. Interact with the UI.
        composeTestRule.onNodeWithText(initialName).performTextClearance()
        composeTestRule.onNodeWithText("").performTextInput(newName)
        composeTestRule.onNodeWithText(saveButtonText).performClick()

        // 4. Assert.
        assertThat(fakeDataStore.playerName.value).isEqualTo(newName)
    }

    @Test
    fun backgroundSelection_canBeUpdated() = runTest {
        val initialSelection = setOf("background_01", "background_03")
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        // 1. Set the desired state BEFORE creating the ViewModel.
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        // 2. Create the ViewModel now, so its `init` block uses the correct state.
        val viewModel = PreferencesViewModel(navController = navController, appSettingsDataStore = fakeDataStore)

        // 3. Set content and wait for UI to stabilize.
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val selectButtonText = context.getString(R.string.preferences_button_select_backgrounds, initialSelection.size)
        val dialogTitle = context.getString(R.string.background_selection_dialog_title)
        val okButtonText = context.getString(R.string.button_ok)

        // 4. Open and interact with the dialog.
        composeTestRule.onNodeWithText(selectButtonText).performScrollTo().performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("Background 01").performClick() // Deselect
        composeTestRule.onNodeWithText("Background 02").performClick() // Select
        composeTestRule.onNodeWithText(okButtonText).performClick()

        // 5. Confirm the selection to trigger the save operation and wait.
        viewModel.confirmBackgroundSelections()
        composeTestRule.waitForIdle()

        // 6. Assert the final state in the DataStore.
        val expectedSelection = setOf("background_03", "background_02")
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(expectedSelection)
    }

    @Test
    fun boardDimensions_showsErrorWhenNotEnoughCards() = runTest {
        val initialWidth = 3
        val initialHeight = 4
        val initialCards = (1..6).map { "img_c_%02d".format(it) }.toSet() // 6 cards

        // 1. Set up initial state with not enough cards for a larger board
        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        // 2. Create the ViewModel and compose the UI
        val viewModel = PreferencesViewModel(navController = navController, appSettingsDataStore = fakeDataStore)
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        // 3. Find the slider by its testTag, scroll to it, and swipe to increase the value
        composeTestRule.onNodeWithTag("WidthSlider").performScrollTo().performGesture { swipeRight() }
        composeTestRule.waitForIdle() // Wait for the ViewModel to process the change

        // 4. Assert that the error is shown and the dimensions were not saved
        assertThat(viewModel.boardDimensionError.value).isNotNull()
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth) // Verify it did not change
    }

    @Test
    fun musicSelectionDialog_opensAndInteractsCorrectly() = runTest {
        // 1. Create the ViewModel for this specific test.
        val viewModel = PreferencesViewModel(navController = navController, appSettingsDataStore = fakeDataStore)

        // 2. Set content and wait for UI to stabilize.
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialCount = viewModel.selectedMusicTrackNames.value.size
        val selectMusicTracksText = context.getString(R.string.preferences_button_select_music_tracks, initialCount)
        val dialogTitle = context.getString(R.string.preferences_music_selection_dialog_title)
        val toggleAllText = context.getString(R.string.dialog_select_deselect_all)
        val okText = context.getString(R.string.button_ok)

        // 3. Interact with the UI.
        composeTestRule.onNodeWithText(selectMusicTracksText).performScrollTo().performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(toggleAllText).performClick()
        composeTestRule.onNodeWithText(toggleAllText).performClick()
        val trackToSelect = "Classic Slow Guitar"
        composeTestRule.onNodeWithText(trackToSelect).performScrollTo().performClick()
        composeTestRule.onNodeWithText(okText).performClick()

        // 4. Assert.
        val expectedSelection = setOf("classic_slow_guitar")
        assertThat(viewModel.selectedMusicTrackNames.value).isEqualTo(expectedSelection)
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()
    }
}