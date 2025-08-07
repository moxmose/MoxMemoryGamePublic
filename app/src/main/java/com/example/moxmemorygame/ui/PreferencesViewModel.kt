package com.example.moxmemorygame.ui

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.AppSettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val navController: NavHostController,
    private val appSettingsDataStore: AppSettingsDataStore // Inietta AppSettingsDataStore
) : ViewModel() {

    val playerName: StateFlow<String> = appSettingsDataStore.playerNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "" // O un valore di caricamento appropriato
        )

    val cardSet: StateFlow<String> = appSettingsDataStore.cardSetFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "default_set"
        )

    val backgroundPreference: StateFlow<String> = appSettingsDataStore.backgroundPreferenceFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "random"
        )

    fun updatePlayerName(newName: String) {
        if(newName.length <= PLAYERNAME_MAX_LENGTH) {
            viewModelScope.launch {
                appSettingsDataStore.savePlayerName(newName)
            }
        }
    }

    fun updateCardSet(newSet: String) {
        viewModelScope.launch {
            appSettingsDataStore.saveCardSet(newSet)
        }
    }

    fun updateBackgroundPreference(newPreference: String) {
        viewModelScope.launch {
            appSettingsDataStore.saveBackgroundPreference(newPreference)
        }
    }

    companion object {
        const val PLAYERNAME_MAX_LENGTH = 20
    }

}