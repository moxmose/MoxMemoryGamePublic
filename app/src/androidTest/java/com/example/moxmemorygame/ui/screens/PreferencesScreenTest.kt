package com.example.moxmemorygame.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.moxmemorygame.R
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.ui.BackgroundMusicManager
import com.example.moxmemorygame.ui.PreferencesViewModel
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
        composeTestRule.onNodeWithText(saveButtonText).performClick()

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

        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(selectButtonText))
        composeTestRule.onNodeWithText(selectButtonText).performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("Background 01").performClick() // Deselect
        composeTestRule.onNodeWithText("Background 02").performClick() // Select
        composeTestRule.onNodeWithText(okButtonText).performClick()

        viewModel.confirmBackgroundSelections()
        composeTestRule.waitForIdle()

        val expectedSelection = setOf("background_03", "background_02")
        assertThat(fakeDataStore.selectedBackgrounds.value).isEqualTo(expectedSelection)
    }

    @Test
    fun boardDimensions_showsErrorWhenNotEnoughCards() = runTest {
        val initialWidth = 3
        val initialHeight = 4
        val initialCards = (1..6).map { "img_c_%02d".format(it) }.toSet()

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasTestTag("WidthSlider"))
        composeTestRule.onNodeWithTag("WidthSlider").performGesture { swipeRight() }
        composeTestRule.waitForIdle()

        assertThat(viewModel.boardDimensionError.value).isNotNull()
        assertThat(fakeDataStore.selectedBoardWidth.value).isEqualTo(initialWidth)
    }

    @Test
    fun cardSelection_disablesConfirmWhenNotEnoughCards() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialWidth = 3
        val initialHeight = 4
        val minRequired = (initialWidth * initialHeight) / 2 // 6
        val initialCards = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        val totalRefinedCards = 20

        fakeDataStore.saveBoardDimensions(initialWidth, initialHeight)
        fakeDataStore.saveSelectedCards(initialCards)

        val viewModel = createViewModel()
        composeTestRule.setContent {
            PreferencesScreen(preferencesViewModel = viewModel, innerPadding = PaddingValues(0.dp))
        }
        composeTestRule.waitForIdle()

        val buttonText = context.getString(R.string.preferences_button_select_refined_cards, initialCards.size, totalRefinedCards)
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(buttonText))
        composeTestRule.onNodeWithText(buttonText).performClick()

        val okButtonNode = composeTestRule.onNodeWithText(context.getString(R.string.button_ok))
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

        val buttonText = context.getString(R.string.preferences_button_select_refined_cards, initialCards.size, totalRefinedCards)
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(buttonText))
        composeTestRule.onNodeWithText(buttonText).performClick()

        composeTestRule.onNodeWithText("Refined 1").performClick() // Deselect
        val newDialogTitle = context.getString(R.string.preferences_button_select_refined_cards, 5, totalRefinedCards)
        composeTestRule.onNodeWithText(newDialogTitle).assertIsDisplayed()

        composeTestRule.onNodeWithText("Refined 7").performClick() // Select
        composeTestRule.onNodeWithText(context.getString(R.string.button_ok)).performClick()

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
        composeTestRule.onNodeWithTag("PreferencesList").performScrollToNode(hasText(selectMusicTracksText))
        composeTestRule.onNodeWithText(selectMusicTracksText).performClick()
        composeTestRule.onNodeWithText(noneButtonText).performClick()
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
        composeTestRule.onNodeWithTag(sfxSliderTag).performGesture { swipeRight() }
        composeTestRule.waitForIdle()
        assertThat(fakeDataStore.soundEffectsVolume.value).isGreaterThan(initialVolume)
    }
}