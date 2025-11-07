package com.example.moxmemorygame

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.model.ScoreEntry
import com.example.moxmemorygame.ui.OpeningMenuViewModel
import com.example.moxmemorygame.ui.Screen
import com.example.moxmemorygame.ui.screens.OpeningMenuScreen
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OpeningMenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickingTitle_opensAboutDialog() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val fakeDataStore = FakeAppSettingsDataStore()
        val viewModel = OpeningMenuViewModel(navController, fakeDataStore)

        composeTestRule.setContent {
            OpeningMenuScreen(
                innerPadding = PaddingValues(0.dp),
                openingMenuViewModel = viewModel
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.onNodeWithText(context.getString(R.string.opening_menu_title)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.about_dialog_title)).assertExists()
    }

    @Test
    fun clickingStartGame_navigatesToGameScreen() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val fakeDataStore = FakeAppSettingsDataStore()
        val viewModel = OpeningMenuViewModel(navController, fakeDataStore)

        composeTestRule.setContent {
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            NavHost(navController = navController, startDestination = Screen.OpeningMenuScreen.route) {
                composable(Screen.OpeningMenuScreen.route) {
                    OpeningMenuScreen(
                        innerPadding = PaddingValues(0.dp),
                        openingMenuViewModel = viewModel
                    )
                }
                composable(Screen.GameScreen.route) { Text("Game Screen") }
                composable(Screen.PreferencesScreen.route) { Text("Preferences Screen") }
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.onNodeWithText(context.getString(R.string.opening_menu_button_start_game)).performClick()

        assertEquals(Screen.GameScreen.route, navController.currentDestination?.route)
    }

    @Test
    fun clickingSettings_navigatesToPreferencesScreen() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val fakeDataStore = FakeAppSettingsDataStore()
        val viewModel = OpeningMenuViewModel(navController, fakeDataStore)

        composeTestRule.setContent {
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            NavHost(navController = navController, startDestination = Screen.OpeningMenuScreen.route) {
                composable(Screen.OpeningMenuScreen.route) {
                    OpeningMenuScreen(
                        innerPadding = PaddingValues(0.dp),
                        openingMenuViewModel = viewModel
                    )
                }
                composable(Screen.GameScreen.route) { Text("Game Screen") }
                composable(Screen.PreferencesScreen.route) { Text("Preferences Screen") }
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.onNodeWithText(context.getString(R.string.opening_menu_button_settings)).performClick()

        assertEquals(Screen.PreferencesScreen.route, navController.currentDestination?.route)
    }

    @Test
    fun whenRankingExists_itIsDisplayed() {
        val fakeDataStore = FakeAppSettingsDataStore()
        runBlocking {
            fakeDataStore.saveScore("Player1", 100)
            fakeDataStore.saveScore("Player2", 200)
        }

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = OpeningMenuViewModel(navController, fakeDataStore)

        composeTestRule.setContent {
            OpeningMenuScreen(
                innerPadding = PaddingValues(0.dp),
                openingMenuViewModel = viewModel
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Verify the "TOP RANKING" title is shown
        composeTestRule.onNodeWithText(context.getString(R.string.opening_menu_top_ranking)).assertIsDisplayed()

        // Verify the RankingList itself is displayed
        composeTestRule.onNodeWithTag("RankingList").assertIsDisplayed()

        // Verify that the Text with "Player2" exists and is a descendant of the list
        composeTestRule.onNode(hasAnyAncestor(hasTestTag("RankingList")).and(hasText("Player2"))).assertIsDisplayed()

        // Verify that the Text with the score exists and is a descendant of the list
        val scoreText = context.getString(R.string.score_points_format, 200)
        composeTestRule.onNode(hasAnyAncestor(hasTestTag("RankingList")).and(hasText(scoreText))).assertIsDisplayed()
    }

    @Test
    fun whenRankingIsFull_itDisplaysLimitedEntries() {
        val fakeDataStore = FakeAppSettingsDataStore()
        val entryCount = ScoreEntry.MAX_RANKING_ENTRIES + 2 // Create more entries than the limit

        runBlocking {
            for (i in 1..entryCount) {
                // Score decreases so the first entries are the highest
                fakeDataStore.saveScore("Player$i", (entryCount - i) * 10)
            }
        }

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = OpeningMenuViewModel(navController, fakeDataStore)

        composeTestRule.setContent {
            OpeningMenuScreen(
                innerPadding = PaddingValues(0.dp),
                openingMenuViewModel = viewModel
            )
        }

        // Verify that the FIRST player that should be visible IS displayed
        composeTestRule.onNode(hasText("Player1")).assertIsDisplayed()

        // Verify that the LAST player that should be visible IS displayed by scrolling to it first
        val lastVisiblePlayerMatcher = hasText("Player${ScoreEntry.MAX_RANKING_ENTRIES}")
        composeTestRule.onNodeWithTag("RankingList").performScrollToNode(lastVisiblePlayerMatcher)
        composeTestRule.onNode(lastVisiblePlayerMatcher.and(hasAnyAncestor(hasTestTag("RankingList")))).assertIsDisplayed()

        // Verify that the FIRST player that should NOT be visible does NOT exist
        val firstInvisiblePlayer = "Player${ScoreEntry.MAX_RANKING_ENTRIES + 1}"
        composeTestRule.onNodeWithText(firstInvisiblePlayer).assertDoesNotExist()
    }
}