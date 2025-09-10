package com.example.moxmemorygame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.moxmemorygame.IAppSettingsDataStore
import com.example.moxmemorygame.RealAppSettingsDataStore // Importato per DEFAULT_SELECTED_CARDS
//import com.example.moxmemorygame.ui.GameCardClass.Companion.BOARD_HEIGHT
//import com.example.moxmemorygame.ui.GameCardClass.Companion.BOARD_WIDTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val navController: NavHostController, // Lasciato come da originale, non commentato
    private val appSettingsDataStore: IAppSettingsDataStore
) : ViewModel() {

    val playerName: StateFlow<String> = appSettingsDataStore.playerName
    // Rimossa val cardSet: StateFlow<String> = appSettingsDataStore.cardSet // Potrebbe necessitare modifiche simili per selezione multipla

    // Constante per il numero minimo di carte richieste per il gioco
    private val minRequiredCards = (BOARD_WIDTH * BOARD_HEIGHT) / 2

    // Lista statica degli sfondi disponibili
    val availableBackgrounds: List<String> = List(7) { i -> "background_%02d".format(i) }

    // Stato per gli sfondi selezionati (osservato dalla UI principale)
    private val _selectedBackgrounds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBackgrounds: StateFlow<Set<String>> = _selectedBackgrounds.asStateFlow()

    // Lista di tutte le carte disponibili come nomi di risorsa
    // img_c_XX: Refined cards (00-19)
    // img_s_XX: Simple cards (00-09)
    val availableCardResourceNames: List<String> = buildList {
        (0..19).forEach { add("img_c_%02d".format(it)) } // Carte "Refined"
        (0..9).forEach { add("img_s_%02d".format(it)) }  // Carte "Simple"
    }

    // Stato per le carte selezionate (osservato dalla UI)
    private val _selectedCards = MutableStateFlow<Set<String>>(emptySet())
    val selectedCards: StateFlow<Set<String>> = _selectedCards.asStateFlow()

    // Stato per messaggi di errore relativi alla selezione delle carte
    private val _cardSelectionError = MutableStateFlow<String?>(null)
    /**
     * A [StateFlow] emitting error messages related to card selection, if any.
     * Null if there is no error.
     */
    val cardSelectionError: StateFlow<String?> = _cardSelectionError.asStateFlow()

    init {
        // Caricamento sfondi selezionati
        viewModelScope.launch {
            val initialSelectedBgs = appSettingsDataStore.selectedBackgrounds.first()
            _selectedBackgrounds.value = if (initialSelectedBgs.isNullOrEmpty()) {
                // Se non ci sono sfondi salvati o il set è vuoto, usa un default (es. tutti o il primo)
                if (availableBackgrounds.isNotEmpty()) {
                    // Default: seleziona tutti gli sfondi disponibili se nessuno è salvato
                    // appSettingsDataStore.saveSelectedBackgrounds(availableBackgrounds.toSet()) // Opzionale: salvare subito il default
                    // availableBackgrounds.toSet()
                    // Modifica: Default al primo sfondo se vuoto, come da logica in RealAppSettingsDataStore
                    val defaultBg = setOf(availableBackgrounds.first())
                    appSettingsDataStore.saveSelectedBackgrounds(defaultBg) // Salva il default se inizialmente vuoto
                    defaultBg
                } else {
                    emptySet()
                }
            } else {
                initialSelectedBgs
            }
        }

        // Caricamento carte selezionate
        viewModelScope.launch {
            val initialSelectedCards = appSettingsDataStore.selectedCards.first()
            // Se le carte salvate sono insufficienti o non presenti, usa il default e salvalo.
            // DEFAULT_SELECTED_CARDS è già definito per contenere tutte le carte "complex" (20 carte), che è >= minRequiredCards (10)
            if (initialSelectedCards.isNullOrEmpty() || initialSelectedCards.size < minRequiredCards) {
                _selectedCards.value = RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS
                appSettingsDataStore.saveSelectedCards(RealAppSettingsDataStore.DEFAULT_SELECTED_CARDS)
            } else {
                // Altrimenti, usa le carte caricate.
                _selectedCards.value = initialSelectedCards
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

    // Rimossa fun updateCardSet(newSet: String) { // Potrebbe necessitare modifiche simili

    /**
     * Converte il nome della risorsa di una carta nel suo nome visualizzabile dall'utente.
     * Esempio: "img_c_01" -> "Refined 01", "img_s_05" -> "Simple 05".
     * I numeri vengono mantenuti come sono dopo il prefisso.
     * @param resourceName Il nome della risorsa della carta (es. "img_c_00").
     * @return Il nome visualizzabile (es. "Refined 00").
     */
    fun getCardDisplayName(resourceName: String): String {
        return when {
            resourceName.startsWith("img_c_") -> "Refined ${resourceName.removePrefix("img_c_").removePrefix("0")}" // Rimuove lo 0 iniziale se presente dopo il prefisso
            resourceName.startsWith("img_s_") -> "Simple ${resourceName.removePrefix("img_s_").removePrefix("0")}"
            else -> resourceName // Fallback, non dovrebbe accadere per nomi noti
        }
    }

    /**
     * Funzione per confermare e salvare le selezioni degli sfondi fatte nel dialogo.
     * Assicura che almeno uno sfondo sia selezionato se disponibili.
     */
    fun confirmBackgroundSelections(confirmedSelection: Set<String>) {
        viewModelScope.launch {
            val finalSelection = if (confirmedSelection.isEmpty() && availableBackgrounds.isNotEmpty()) {
                // Assicura che almeno uno sfondo sia selezionato, se confermata selezione vuota ma sfondi disponibili
                setOf(availableBackgrounds.first())
            } else {
                confirmedSelection
            }
            _selectedBackgrounds.value = finalSelection
            appSettingsDataStore.saveSelectedBackgrounds(finalSelection) 
        }
    }

    // La vecchia funzione `confirmCardSelections` è stata sostituita da `confirmCardSelectionsForSet`
    // /**
    //  * Funzione per confermare e salvare le selezioni delle carte fatte nel dialogo.
    //  * La validazione del numero minimo di carte ([minRequiredCards]) è responsabilità del dialogo chiamante.
    //  * @param confirmedSelection Il set di nomi di risorse delle carte selezionate dall'utente.
    //  */
    // fun confirmCardSelections(confirmedSelection: Set<String>) {
    //     // Si assume che confirmedSelection rispetti già minRequiredCards grazie alla validazione nel Dialog
    //     viewModelScope.launch {
    //         _selectedCards.value = confirmedSelection
    //         appSettingsDataStore.saveSelectedCards(confirmedSelection)
    //     }
    // }

    /**
     * Tenta di confermare e salvare le selezioni per un tipo specifico di carte (Refined o Simple).
     * La selezione viene salvata solo se il numero totale di carte selezionate (combinando entrambi i tipi)
     * è uguale o superiore a [minRequiredCards].
     *
     * @param newlySelectedCardsOfSpecificType Le carte selezionate DALL'UTENTE per il tipo specifico (es. solo Refined).
     * @param cardTypePrefix Il prefisso che identifica il tipo di carte aggiornate (es. "img_c_" o "img_s_").
     */
    fun confirmCardSelectionsForSet(newlySelectedCardsOfSpecificType: Set<String>, cardTypePrefix: String) {
        viewModelScope.launch {
            // 1. Prendi tutte le carte attualmente selezionate che NON sono del tipo che stiamo aggiornando.
            val otherTypeSelectedCards = _selectedCards.value.filterNot { it.startsWith(cardTypePrefix) }.toSet()
            // 2. Combina le selezioni dell'altro tipo con le nuove selezioni per il tipo corrente.
            val potentialNewGlobalSelection = otherTypeSelectedCards + newlySelectedCardsOfSpecificType

            // 3. Valida contro il minimo richiesto.
            if (potentialNewGlobalSelection.size >= minRequiredCards) {
                _selectedCards.value = potentialNewGlobalSelection
                appSettingsDataStore.saveSelectedCards(potentialNewGlobalSelection)
                _cardSelectionError.value = null // Resetta errore se la selezione è valida
            } else {
                // La selezione non è valida (troppo poche carte in totale)
                // Non salvare e imposta un messaggio di errore.
                _cardSelectionError.value = "Minimum $minRequiredCards cards required in total. Current selection has ${potentialNewGlobalSelection.size}. Please select more cards."
                // NON aggiorniamo _selectedCards.value, così la UI esterna non riflette una selezione parziale/non valida globalmente.
            }
        }
    }

    /**
     * Cancella qualsiasi messaggio di errore relativo alla selezione delle carte.
     * Chiamare dopo che l'errore è stato mostrato all'utente.
     */
    fun clearCardSelectionError() {
        _cardSelectionError.value = null
    }

    companion object {
        const val PLAYERNAME_MAX_LENGTH = 20
    }
}