package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.BackgroundMusic
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val navController: NavHostController,
    val appSettingsDataStore: IAppSettingsDataStore,
    private val backgroundMusicManager: BackgroundMusicManager
) : ViewModel() {

    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    val availableBackgrounds: List<String> = List(7) { i -> "background_%02d".format(i) }

    private val _selectedBackgrounds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    private var backgroundSelectionFallback: String? = null

    val availableCardResourceNames: List<String> = buildList {
        (0..19).forEach { add("img_c_%02d".format(it)) } // 20 "refined" cards
        (0..9).forEach { add("img_s_%02d".format(it)) }  // 10 "simple" cards
    }

    val selectedCards: StateFlow<Set<String>> = appSettingsDataStore.selectedCards

    private val _tempSelectedCards = MutableStateFlow<Set<String>>(emptySet())
    val tempSelectedCards: StateFlow<Set<String>> = _tempSelectedCards.asStateFlow()

    val selectedBoardWidth: StateFlow<Int> = appSettingsDataStore.selectedBoardWidth
    val selectedBoardHeight: StateFlow<Int> = appSettingsDataStore.selectedBoardHeight

    private val _cardSelectionError = MutableStateFlow<String?>(null)
    val cardSelectionError: StateFlow<String?> = _cardSelectionError.asStateFlow()

    private val _boardDimensionError = MutableStateFlow<String?>(null)
    val boardDimensionError: StateFlow<String?> = _boardDimensionError.asStateFlow()

    // Music Preferences
    val isMusicEnabled: StateFlow<Boolean> = appSettingsDataStore.isMusicEnabled
    val musicVolume: StateFlow<Float> = appSettingsDataStore.musicVolume
    val selectedMusicTrackNames: StateFlow<Set<String>> = appSettingsDataStore.selectedMusicTrackNames

    // Sound Effects Preferences
    val areSoundEffectsEnabled: StateFlow<Boolean> = appSettingsDataStore.areSoundEffectsEnabled
    val soundEffectsVolume: StateFlow<Float> = appSettingsDataStore.soundEffectsVolume

    private var lastSaveCardsJob: Job? = null
    private var lastSaveBackgroundsJob: Job? = null
    private var lastSaveDimensionsJob: Job? = null
    private var lastSaveMusicJob: Job? = null
    private var lastSaveSfxJob: Job? = null

    init {
        viewModelScope.launch {
            appSettingsDataStore.isDataLoaded.filter { it }.first()

            _selectedBackgrounds.value = appSettingsDataStore.selectedBackgrounds.first()

            val initialBoardWidth = selectedBoardWidth.first()
            val initialBoardHeight = selectedBoardHeight.first()
            val initialMinRequiredPairs = (initialBoardWidth * initialBoardHeight) / 2
            val currentCardsFromDataStore = appSettingsDataStore.selectedCards.first()

            if (currentCardsFromDataStore.size < initialMinRequiredPairs) {
                appSettingsDataStore.saveSelectedCards(IAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
            }
            _tempSelectedCards.value = appSettingsDataStore.selectedCards.first()
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

    fun prepareForBackgroundSelection() {
        val currentSelection = _selectedBackgrounds.value
        backgroundSelectionFallback = availableBackgrounds.firstOrNull { it in currentSelection } ?: availableBackgrounds.first()
    }

    fun updateBackgroundSelection(backgroundName: String, isSelected: Boolean) {
        val currentSelection = _selectedBackgrounds.value.toMutableSet()
        if (isSelected) {
            currentSelection.add(backgroundName)
        } else {
            if (currentSelection.size > 1) { // Prevents deselection of the last item
                currentSelection.remove(backgroundName)
            }
        }
        _selectedBackgrounds.value = currentSelection
    }

    fun toggleSelectAllBackgrounds(selectAll: Boolean) {
        if (selectAll) {
            _selectedBackgrounds.value = availableBackgrounds.toSet()
        } else {
            backgroundSelectionFallback?.let {
                _selectedBackgrounds.value = setOf(it)
            }
        }
    }

    fun confirmBackgroundSelections() {
        lastSaveBackgroundsJob = viewModelScope.launch {
            appSettingsDataStore.saveSelectedBackgrounds(_selectedBackgrounds.value)
        }
    }

    fun prepareForCardSelection() {
        _tempSelectedCards.value = selectedCards.value
    }

    fun confirmCardSelections() {
        val minRequiredPairs = (selectedBoardWidth.value * selectedBoardHeight.value) / 2
        if (_tempSelectedCards.value.size >= minRequiredPairs) {
            _cardSelectionError.value = null
            lastSaveCardsJob = viewModelScope.launch {
                appSettingsDataStore.saveSelectedCards(_tempSelectedCards.value)
            }
        } else {
            _cardSelectionError.value = "Minimum $minRequiredPairs cards required for the current board size. You have selected ${_tempSelectedCards.value.size}."
        }
    }

    fun updateCardSelection(cardName: String, isSelected: Boolean) {
        val currentSelection = _tempSelectedCards.value.toMutableSet()
        if (isSelected) {
            currentSelection.add(cardName)
        } else {
            currentSelection.remove(cardName)
        }
        _tempSelectedCards.value = currentSelection
    }

    fun toggleSelectAllCards(cardSet: List<String>, selectAll: Boolean) {
        val currentSelection = _tempSelectedCards.value.toMutableSet()
        if (selectAll) {
            currentSelection.addAll(cardSet)
        } else {
            currentSelection.removeAll(cardSet.toSet())
        }
        _tempSelectedCards.value = currentSelection
    }

    fun clearCardSelectionError() {
        _cardSelectionError.value = null
    }

    fun updateBoardDimensions(newWidth: Int, newHeight: Int) {
        val currentSelectedCardsCount = selectedCards.value.size
        val requiredPairs = (newWidth * newHeight) / 2

        if (newWidth < MIN_BOARD_WIDTH || newWidth > MAX_BOARD_WIDTH || newHeight < MIN_BOARD_HEIGHT || newHeight > MAX_BOARD_HEIGHT || (newWidth * newHeight) % 2 != 0) {
            _boardDimensionError.value = "Invalid dimensions."
            return
        }

        if (currentSelectedCardsCount < requiredPairs) {
            _boardDimensionError.value = "Board size ${newWidth}x${newHeight} requires $requiredPairs pairs. You have $currentSelectedCardsCount."
            return
        }

        _boardDimensionError.value = null
        lastSaveDimensionsJob = viewModelScope.launch {
            appSettingsDataStore.saveBoardDimensions(newWidth, newHeight)
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
            lastSaveMusicJob?.join()
            lastSaveSfxJob?.join()
            backgroundMusicManager.stopPreview() // Ensure preview is stopped
            navController.popBackStack()
        }
    }

    fun saveIsMusicEnabled(isEnabled: Boolean) {
        lastSaveMusicJob = viewModelScope.launch {
            appSettingsDataStore.saveIsMusicEnabled(isEnabled)
        }
    }

    fun saveMusicVolume(volume: Float) {
        lastSaveMusicJob = viewModelScope.launch {
            appSettingsDataStore.saveMusicVolume(volume)
        }
    }

    fun saveSelectedMusicTracks(trackNames: Set<String>) {
        lastSaveMusicJob = viewModelScope.launch {
            appSettingsDataStore.saveSelectedMusicTracks(trackNames)
        }
    }

    fun saveAreSoundEffectsEnabled(isEnabled: Boolean) {
        lastSaveSfxJob = viewModelScope.launch {
            appSettingsDataStore.saveAreSoundEffectsEnabled(isEnabled)
        }
    }

    fun saveSoundEffectsVolume(volume: Float) {
        lastSaveSfxJob = viewModelScope.launch {
            appSettingsDataStore.saveSoundEffectsVolume(volume)
        }
    }

    fun playMusicPreview(track: BackgroundMusic) {
        backgroundMusicManager.playPreview(track)
    }

    fun stopMusicPreview() {
        backgroundMusicManager.stopPreview()
    }

    override fun onCleared() {
        super.onCleared()
        backgroundMusicManager.stopPreview()
    }

    companion object {
        const val PLAYERNAME_MAX_LENGTH = 20
        const val MIN_BOARD_WIDTH = 3
        const val MAX_BOARD_WIDTH = 5
        const val MIN_BOARD_HEIGHT = 4
        const val MAX_BOARD_HEIGHT = 6
    }
}
