package com.example.moxmemorygame.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.OpeningMenuScreen.route
    ) {
        composable(Screen.OpeningMenuScreen.route) {
            OpeningMenuScreen(navController = navController)
        }
        composable(Screen.PreferencesScreen.route) {
            PreferencesScreen(navController = navController)
        }
        composable(Screen.GameScreen.route) {
            GameScreen(navController = navController)
        }
    }

}