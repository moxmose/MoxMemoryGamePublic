package com.example.moxmemorygame.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.TestGameScreen
import com.example.moxmemorygame.TestOpeningMenuScreen
import com.example.moxmemorygame.TestPreferencesScreen

@Composable
fun NavGraph(
    innerPadding: PaddingValues,
    navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.OpeningMenuScreen.route
    ) {
        composable(Screen.OpeningMenuScreen.route) {
            TestOpeningMenuScreen(navController = navController, innerPadding = innerPadding)
        }
        composable(Screen.PreferencesScreen.route) {
            TestPreferencesScreen(navController = navController, innerPadding = innerPadding)
        }
        composable(Screen.GameScreen.route) {
            TestGameScreen(navController = navController, innerPadding = innerPadding)
        }
    }

}