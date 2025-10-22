package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
// Correct import for IAppSettingsDataStore
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.flow.StateFlow

class OpeningMenuViewModel(
    private val navController: NavHostController,
    private val appSettingsDataStore: IAppSettingsDataStore // Now uses the version from data.local
) : ViewModel() {

    // Navigation logic
    fun onStartGameClicked() {
        navController.navigate(Screen.GameScreen.route)
    }

    fun onSettingsClicked() {
        navController.navigate(Screen.PreferencesScreen.route)
    }

    // Preferences
    val backgroundPreference: StateFlow<Set<String>> = appSettingsDataStore.selectedBackgrounds

    // Ranking and Last Played Data
    val topRanking: StateFlow<List<ScoreEntry>> = appSettingsDataStore.topRanking
    val lastPlayedEntry: StateFlow<ScoreEntry?> = appSettingsDataStore.lastPlayedEntry

    // Example of other navigation if needed, not directly used by OpeningMenuScreen buttons
    fun navigate(destination: String) {
        navController.navigate(destination)
    }
    fun goBack(){
        navController.popBackStack()
    }
}