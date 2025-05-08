package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController

class OpeningMenuViewModel(private val navController: NavHostController) : ViewModel() {

    fun navigate(destination: String) {
        navController.navigate(destination)
    }
    fun goBack(){
        navController.popBackStack()
    }

    // viewModel logic
    fun onStartGameClicked() {
        navController.navigate(Screen.GameScreen.route)
    }

    fun onSettingsClicked() {
        navController.navigate(Screen.PreferencesScreen.route)
    }

}