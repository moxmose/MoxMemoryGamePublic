package com.example.moxmemorygame.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.ui.screens.GameScreen
import com.example.moxmemorygame.ui.screens.OpeningMenuScreen
import com.example.moxmemorygame.ui.screens.PreferencesScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.context.GlobalContext

@Composable
fun NavGraph(
    innerPadding: PaddingValues,
    navController: NavHostController = rememberNavController(),

) {
    // get the koin instance from the Koin Global Context
    val koin = GlobalContext.get()
    // get the NavigationManager from the Koin instance
    val navigationManager: NavigationManager = koin.get()
    // tell to NavigationManager to use the navController
    navigationManager.setNavController(navController)

    NavHost(
        navController = navController,
        startDestination = Screen.OpeningMenuScreen.route,
    ) {
        composable(Screen.OpeningMenuScreen.route) {
            val viewModel: OpeningMenuViewModel = koinViewModel {
                    parametersOf(navController) }
            OpeningMenuScreen(viewModel, innerPadding)
        }

        composable(Screen.PreferencesScreen.route) {
//            TestPreferencesScreen(navController = navController, innerPadding = innerPadding)
            val viewModel: PreferencesViewModel = koinViewModel { parametersOf(navController) }
            PreferencesScreen(  preferencesViewModel = viewModel  , innerPadding = innerPadding)
        }

        composable(Screen.GameScreen.route) {
            val viewModel: GameViewModel = koinViewModel { parametersOf(navController) }
            GameScreen(gameViewModel = viewModel, //navController,
                innerPadding = innerPadding)
            //TestGameScreen(navController = navController, innerPadding = innerPadding)
        }
    }

}