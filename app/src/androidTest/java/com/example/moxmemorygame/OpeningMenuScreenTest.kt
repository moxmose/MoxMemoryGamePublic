package com.example.moxmemorygame

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.ui.OpeningMenuViewModel
import com.example.moxmemorygame.ui.Screen
import com.example.moxmemorygame.ui.screens.OpeningMenuScreen
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
}