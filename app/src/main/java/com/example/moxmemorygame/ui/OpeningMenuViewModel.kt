package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.moxmemorygame.IAppSettingsDataStore
import com.example.moxmemorygame.RealAppSettingsDataStore
import kotlinx.coroutines.flow.StateFlow

class OpeningMenuViewModel(
    private val navController: NavHostController,
//    private val appSettingsDataStore: RealAppSettingsDataStore // Inject AppSettingsDataStore
    private val appSettingsDataStore: IAppSettingsDataStore // Inject AppSettingsDataStore
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

    val backgroundPreference: StateFlow<Set<String>> = appSettingsDataStore.selectedBackgrounds
    // ... altra logica

}