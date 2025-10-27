package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.ScoreEntry
import kotlinx.coroutines.flow.StateFlow

class OpeningMenuViewModel(
    private val navController: NavHostController,
    private val appSettingsDataStore: IAppSettingsDataStore
) : ViewModel() {

    val topRanking: StateFlow<List<ScoreEntry>> = appSettingsDataStore.topRanking
    val lastPlayedEntry: StateFlow<ScoreEntry?> = appSettingsDataStore.lastPlayedEntry
    val selectedBackgrounds: StateFlow<Set<String>> = appSettingsDataStore.selectedBackgrounds

    fun onStartGameClicked() {
        navController.navigate(Screen.GameScreen.route)
    }

    fun onSettingsClicked() {
        navController.navigate(Screen.PreferencesScreen.route)
    }
}