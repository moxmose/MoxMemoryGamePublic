package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.IAppSettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val navController: NavHostController,
    private val appSettingsDataStore: IAppSettingsDataStore
) : ViewModel() {

    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    val cardSet: StateFlow<String> = appSettingsDataStore.cardSet // Potrebbe necessitare modifiche simili per selezione multipla

    // Lista statica degli sfondi disponibili
    val availableBackgrounds: List<String> = List(7) { i -> "background_%02d".format(i) }

    // Stato per gli sfondi selezionati (osservato dalla UI principale)
    private val _selectedBackgrounds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    init {
        viewModelScope.launch {
            val initialSelected = appSettingsDataStore.selectedBackgrounds.first()
            if (initialSelected.isEmpty()) {
                // Se non ci sono sfondi salvati, seleziona tutti quelli disponibili come default
                if (availableBackgrounds.isNotEmpty()) { // Assicurati che ci siano sfondi disponibili
                    _selectedBackgrounds.value = availableBackgrounds.toSet()
                    appSettingsDataStore.saveSelectedBackgrounds(_selectedBackgrounds.value)
                } else {
                    // Fallback nel caso (improbabile qui) availableBackgrounds sia vuoto
                    _selectedBackgrounds.value = emptySet()
                    // Non salvare un set vuoto se la logica richiede almeno uno selezionato altrove
                }
            } else {
                _selectedBackgrounds.value = initialSelected
            }
        }
    }

    fun updatePlayerName(newName: String) {
        if (newName.length <= PLAYERNAME_MAX_LENGTH) {
            viewModelScope.launch {
                appSettingsDataStore.savePlayerName(newName)
            }
        }
    }

    fun updateCardSet(newSet: String) { // Potrebbe necessitare modifiche simili
        viewModelScope.launch {
            appSettingsDataStore.saveCardSet(newSet)
        }
    }

    // Funzione per confermare e salvare le selezioni degli sfondi fatte nel dialogo
    fun confirmBackgroundSelections(confirmedSelection: Set<String>) {
        viewModelScope.launch {
            val finalSelection = if (confirmedSelection.isEmpty() && availableBackgrounds.isNotEmpty()) {
                // Assicura che almeno uno sfondo sia selezionato, anche se il dialogo dovrebbe già farlo
                // Se la selezione confermata è vuota e ci sono sfondi disponibili, seleziona il primo.
                setOf(availableBackgrounds.first())
            } else if (confirmedSelection.isEmpty() && availableBackgrounds.isEmpty()){
                // Se la selezione confermata è vuota E non ci sono sfondi disponibili, resta vuota.
                emptySet()
            } else {
                confirmedSelection
            }
            _selectedBackgrounds.value = finalSelection
            // Salva solo se la selezione finale non è un set vuoto che viola una possibile regola "almeno uno"
            // Tuttavia, la logica attuale di selezione di default nel blocco init e nel dialogo cerca di evitarlo.
            appSettingsDataStore.saveSelectedBackgrounds(finalSelection) 
        }
    }

    companion object {
        const val PLAYERNAME_MAX_LENGTH = 20
    }
}