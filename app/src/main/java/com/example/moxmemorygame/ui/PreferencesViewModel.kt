package com.example.moxmemorygame.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.data.local.RealAppSettingsDataStore // Per i valori di default
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val navController: NavHostController,
    val appSettingsDataStore: IAppSettingsDataStore // Reso pubblico per accesso diretto dalla UI
) : ViewModel() {

    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    val availableBackgrounds: List<String> = List(7) { i -> "background_%02d".format(i) }

    private val _selectedBackgrounds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    val availableCardResourceNames: List<String> = buildList {
        (0..19).forEach { add("img_c_%02d".format(it)) } // 20 carte "refined"
        (0..9).forEach { add("img_s_%02d".format(it)) }  // 10 carte "simple"
    }

    // StateFlow per le dimensioni della tabella, direttamente da DataStore
    val selectedBoardWidth: StateFlow<Int> = appSettingsDataStore.selectedBoardWidth
    val selectedBoardHeight: StateFlow<Int> = appSettingsDataStore.selectedBoardHeight

    private val _cardSelectionError = MutableStateFlow<String?>(null)
    val cardSelectionError: StateFlow<String?> = _cardSelectionError.asStateFlow()

    private val _boardDimensionError = MutableStateFlow<String?>(null)
    val boardDimensionError: StateFlow<String?> = _boardDimensionError.asStateFlow()

    private var lastSaveCardsJob: Job? = null
    private var lastSaveBackgroundsJob: Job? = null
    private var lastSaveDimensionsJob: Job? = null

    init {
        Log.d("PrefVM_Init", "Initializing PreferencesViewModel")
        viewModelScope.launch {
            Log.d("PrefVM_Init", "Waiting for DataStore to be loaded...")
            appSettingsDataStore.isDataLoaded.filter { it }.first()
            Log.d("PrefVM_Init", "DataStore is loaded.")

            val initialSelectedBgs = appSettingsDataStore.selectedBackgrounds.first()
            _selectedBackgrounds.value = if (initialSelectedBgs.isNullOrEmpty()) {
                if (availableBackgrounds.isNotEmpty()) setOf(availableBackgrounds.first()) else emptySet()
            } else {
                initialSelectedBgs
            }
            Log.d("PrefVM_Init", "Initialized _selectedBackgrounds.value: ${_selectedBackgrounds.value}")

            // Validazione iniziale carte basata sulle dimensioni iniziali della tabella
            val initialBoardWidth = selectedBoardWidth.first()
            val initialBoardHeight = selectedBoardHeight.first()
            val initialMinRequiredPairs = (initialBoardWidth * initialBoardHeight) / 2
            val currentCardsFromDataStore = appSettingsDataStore.selectedCards.first()

            if (currentCardsFromDataStore.isNullOrEmpty() || currentCardsFromDataStore.size < initialMinRequiredPairs) {
                Log.w("PrefVM_Init", "Cards from DataStore (count: ${currentCardsFromDataStore?.size}) insufficient for initial board size ($initialBoardWidth x $initialBoardHeight requires $initialMinRequiredPairs pairs). ViewModel will rely on DataStore defaults or user selection.")
                // RealAppSettingsDataStore dovrebbe già fornire DEFAULT_SELECTED_CARDS se necessario.
            } else {
                Log.d("PrefVM_Init", "Initial cards from DataStore seem valid for initial board size.")
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
            _selectedBackgrounds.value = finalSelection
            appSettingsDataStore.saveSelectedBackgrounds(finalSelection)
        }
    }

    fun confirmCardSelectionsForSet(newlySelectedCardsOfSpecificType: Set<String>, cardTypePrefix: String) {
        val currentGlobalSelectedCards = appSettingsDataStore.selectedCards.value 
        val otherTypeSelectedCards = currentGlobalSelectedCards.filterNot { it.startsWith(cardTypePrefix) }.toSet()
        val potentialNewGlobalSelection = otherTypeSelectedCards + newlySelectedCardsOfSpecificType
        
        val currentMinRequiredPairs = (selectedBoardWidth.value * selectedBoardHeight.value) / 2

        if (potentialNewGlobalSelection.size >= currentMinRequiredPairs) {
            Log.d("PrefVM", "confirmCardSelectionsForSet - Valid selection. Saving: $potentialNewGlobalSelection")
            lastSaveCardsJob = viewModelScope.launch {
                appSettingsDataStore.saveSelectedCards(potentialNewGlobalSelection)
                _cardSelectionError.value = null
            }
        } else {
            Log.w("PrefVM", "confirmCardSelectionsForSet - Invalid selection. Needed $currentMinRequiredPairs pairs for board ${selectedBoardWidth.value}x${selectedBoardHeight.value}, got ${potentialNewGlobalSelection.size}")
            _cardSelectionError.value = "Minimum $currentMinRequiredPairs cards required for the current board size (${selectedBoardWidth.value}x${selectedBoardHeight.value}). Current selection has ${potentialNewGlobalSelection.size}."
        }
    }

    fun clearCardSelectionError() {
        _cardSelectionError.value = null
    }

    fun updateBoardDimensions(newWidth: Int, newHeight: Int) {
        val currentSelectedCardsCount = appSettingsDataStore.selectedCards.value.size
        val requiredPairs = (newWidth * newHeight) / 2

        if (newWidth < MIN_BOARD_WIDTH || newWidth > MAX_BOARD_WIDTH) {
            _boardDimensionError.value = "Width must be between $MIN_BOARD_WIDTH and $MAX_BOARD_WIDTH."
            return
        }
        if (newHeight < MIN_BOARD_HEIGHT || newHeight > MAX_BOARD_HEIGHT) {
            _boardDimensionError.value = "Height must be between $MIN_BOARD_HEIGHT and $MAX_BOARD_HEIGHT."
            return
        }
        if ((newWidth * newHeight) % 2 != 0) {
            _boardDimensionError.value = "Total number of cells (Width x Height) must be even."
            return // Già coperto dai range, ma una doppia verifica non guasta
        }
        if (currentSelectedCardsCount < requiredPairs) {
            _boardDimensionError.value = "Selected board size ${newWidth}x${newHeight} requires $requiredPairs unique card pairs. You currently have $currentSelectedCardsCount selected. Please select more cards or reduce board size."
            return
        }

        _boardDimensionError.value = null // Se tutte le validazioni passano
        lastSaveDimensionsJob = viewModelScope.launch {
            appSettingsDataStore.saveBoardDimensions(newWidth, newHeight)
            Log.d("PrefVM", "Saved new board dimensions: ${newWidth}x${newHeight}")
        }
    }

    fun clearBoardDimensionError() {
        _boardDimensionError.value = null
    }

    fun onBackToMainMenuClicked() {
        viewModelScope.launch {
            lastSaveBackgroundsJob?.join()
            lastSaveCardsJob?.join()
            lastSaveDimensionsJob?.join()
            navController.popBackStack()
        }
    }

    companion object {
        const val PLAYERNAME_MAX_LENGTH = 20
        const val MIN_BOARD_WIDTH = 3
        const val MAX_BOARD_WIDTH = 5
        const val MIN_BOARD_HEIGHT = 4
        const val MAX_BOARD_HEIGHT = 6
    }
}
