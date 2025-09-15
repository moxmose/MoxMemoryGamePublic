package com.example.moxmemorygame.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.IAppSettingsDataStore
import com.example.moxmemorygame.RealAppSettingsDataStore // Importato per DEFAULT_SELECTED_CARDS
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter // Necessario per isDataLoaded.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val navController: NavHostController,
    val appSettingsDataStore: IAppSettingsDataStore // Reso pubblico per accesso diretto dalla UI
) : ViewModel() {

    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    private val minRequiredCards = (BOARD_WIDTH * BOARD_HEIGHT) / 2
    val availableBackgrounds: List<String> = List(7) { i -> "background_%02d".format(i) }

    // _selectedBackgrounds rimane gestito internamente per ora, dato che sembra funzionare, 
    // ma potrebbe seguire lo stesso pattern di selectedCards se necessario.
    private val _selectedBackgrounds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    val availableCardResourceNames: List<String> = buildList {
        (0..19).forEach { add("img_c_%02d".format(it)) }
        (0..9).forEach { add("img_s_%02d".format(it)) }
    }

    // selectedCards ora è direttamente appSettingsDataStore.selectedCards
    // Non c'è più un _selectedCards privato in PreferencesViewModel.
    // La UI osserverà appSettingsDataStore.selectedCards direttamente.

    private val _cardSelectionError = MutableStateFlow<String?>(null)
    val cardSelectionError: StateFlow<String?> = _cardSelectionError.asStateFlow()

    private var lastSaveCardsJob: Job? = null
    private var lastSaveBackgroundsJob: Job? = null

    init {
        Log.d("PrefVM_Init", "Initializing PreferencesViewModel")
        viewModelScope.launch {
            Log.d("PrefVM_Init", "Waiting for DataStore to be loaded before loading initial preferences...")
            appSettingsDataStore.isDataLoaded.filter { it }.first()
            Log.d("PrefVM_Init", "DataStore is loaded. Initializing local flows if necessary.")

            // Caricamento sfondi selezionati (mantenendo la logica attuale per _selectedBackgrounds)
            val initialSelectedBgs = appSettingsDataStore.selectedBackgrounds.first()
            Log.d("PrefVM_Init", "Initial backgrounds from DataStore: $initialSelectedBgs")
            _selectedBackgrounds.value = if (initialSelectedBgs.isNullOrEmpty()) {
                if (availableBackgrounds.isNotEmpty()) setOf(availableBackgrounds.first()) else emptySet()
            } else {
                initialSelectedBgs
            }
            Log.d("PrefVM_Init", "Initialized _selectedBackgrounds.value: ${_selectedBackgrounds.value}")

            // Per selectedCards, non è più necessario inizializzare un flow locale qui,
            // la UI osserverà direttamente appSettingsDataStore.selectedCards.
            // Tuttavia, potremmo voler validare/impostare un default se DataStore è completamente vuoto al primo avvio assoluto dell'app.
            // Ma RealAppSettingsDataStore dovrebbe già gestire questo con il suo initialValue e la logica nel .map.
            // Verifichiamo se il valore iniziale da DataStore per le carte è valido.
            val currentCardsFromDataStore = appSettingsDataStore.selectedCards.first()
            if (currentCardsFromDataStore.isNullOrEmpty() || currentCardsFromDataStore.size < minRequiredCards) {
                Log.w("PrefVM_Init", "Cards from DataStore are null, empty, or insufficient (found: ${currentCardsFromDataStore?.size}, needed: $minRequiredCards). Ensuring default cards are saved if this is the first ever launch.")
                // Questo blocco ora serve principalmente a garantire che, se DataStore fosse VERAMENTE vuoto
                // e la sua logica di default interna non fosse scattata per qualche motivo (improbabile),
                // si tenti di salvare un set di default.
                // In condizioni normali, appSettingsDataStore.selectedCards dovrebbe già fornire DEFAULT_SELECTED_CARDS.
                if (appSettingsDataStore.selectedCards.value == RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS || appSettingsDataStore.selectedCards.value.isEmpty()){ // Controllo più esplicito
                     Log.d("PrefVM_Init", "DataStore cards are default or empty, attempting to save default if truly needed.")
                     // appSettingsDataStore.saveSelectedCards(RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS) // Commentato per evitare sovrascritture non necessarie se R ADS gestisce il default
                }
            } else {
                Log.d("PrefVM_Init", "Cards from DataStore seem valid: $currentCardsFromDataStore")
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

    fun getCardDisplayName(resourceName: String): String {
        return when {
            resourceName.startsWith("img_c_") -> "Refined ${resourceName.removePrefix("img_c_").removePrefix("0")}"
            resourceName.startsWith("img_s_") -> "Simple ${resourceName.removePrefix("img_s_").removePrefix("0")}"
            else -> resourceName
        }
    }

    fun confirmBackgroundSelections(confirmedSelection: Set<String>) {
        lastSaveBackgroundsJob = viewModelScope.launch { 
            val finalSelection = if (confirmedSelection.isEmpty() && availableBackgrounds.isNotEmpty()) {
                setOf(availableBackgrounds.first())
            } else {
                confirmedSelection
            }
            _selectedBackgrounds.value = finalSelection // Aggiorna il flow locale per la UI immediata del dialogo
            Log.d("PrefVM", "confirmBackgroundSelections - Attempting to save to DataStore: $finalSelection")
            appSettingsDataStore.saveSelectedBackgrounds(finalSelection)
            Log.d("PrefVM", "confirmBackgroundSelections - Finished appSettingsDataStore.saveSelectedBackgrounds call for: $finalSelection")
        }
    }

    fun confirmCardSelectionsForSet(newlySelectedCardsOfSpecificType: Set<String>, cardTypePrefix: String) {
        // Legge lo stato corrente direttamente da appSettingsDataStore.selectedCards.value
        val currentGlobalSelectedCards = appSettingsDataStore.selectedCards.value 
        val otherTypeSelectedCards = currentGlobalSelectedCards.filterNot { it.startsWith(cardTypePrefix) }.toSet()
        val potentialNewGlobalSelection = otherTypeSelectedCards + newlySelectedCardsOfSpecificType

        if (potentialNewGlobalSelection.size >= minRequiredCards) {
            Log.d("PrefVM", "confirmCardSelectionsForSet - Valid selection. Saving: $potentialNewGlobalSelection")
            lastSaveCardsJob = viewModelScope.launch {
                // Non c'è più _selectedCards.value da aggiornare qui.
                // Il salvataggio in DataStore triggererà l'aggiornamento dello StateFlow osservato dalla UI.
                appSettingsDataStore.saveSelectedCards(potentialNewGlobalSelection)
                Log.d("PrefVM", "confirmCardSelectionsForSet - Finished appSettingsDataStore.saveSelectedCards call for: $potentialNewGlobalSelection")
                _cardSelectionError.value = null
            }
        } else {
            Log.w("PrefVM", "confirmCardSelectionsForSet - Invalid selection. Needed $minRequiredCards, got ${potentialNewGlobalSelection.size}")
            _cardSelectionError.value = "Minimum $minRequiredCards cards required in total. Current selection has ${potentialNewGlobalSelection.size}. Please select more cards."
        }
    }

    fun clearCardSelectionError() {
        _cardSelectionError.value = null
    }

    fun onBackToMainMenuClicked() {
        viewModelScope.launch {
            Log.d("PrefVM", "onBackToMainMenuClicked - Checking if save jobs need to be joined.")
            lastSaveBackgroundsJob?.join()
            lastSaveCardsJob?.join()
            Log.d("PrefVM", "onBackToMainMenuClicked - Save jobs completed or were null. Navigating back.")
            navController.popBackStack()
        }
    }

    companion object {
        const val PLAYERNAME_MAX_LENGTH = 20
    }
}