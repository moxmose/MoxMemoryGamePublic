package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.moxmemorygame.AppSettingsDataStore
import kotlinx.coroutines.flow.StateFlow

class OpeningMenuViewModel(
    private val navController: NavHostController,
    private val appSettingsDataStore: AppSettingsDataStore // Inject AppSettingsDataStore
) : ViewModel() {

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

    val backgroundPreference: StateFlow<String> = appSettingsDataStore.backgroundPreference
    // ... altra logica

}