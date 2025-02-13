package com.example.moxmemorygame.ui

sealed class Screen(val route: String) {
    object OpeningMenuScreen : Screen("opening_menu_screen")
    object PreferencesScreen : Screen("preferences_screen")
    object GameScreen : Screen("game_screen")
    //object GameOverScreen : Screen("game_over_screen")
    //object WinScreen : Screen("win_screen")
}

