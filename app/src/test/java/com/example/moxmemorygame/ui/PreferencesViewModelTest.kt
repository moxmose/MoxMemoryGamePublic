package com.example.moxmemorygame.ui

import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.Mockito.mock

// Definiamo una regola per gestire le coroutine nei test
@ExperimentalCoroutinesApi
class MainCoroutineRule(private val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class PreferencesViewModelTest {

    // Applica la regola per le coroutine a tutti i test in questa classe
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: PreferencesViewModel
    private lateinit var fakeDataStore: FakeAppSettingsDataStore
    private lateinit var mockNavController: NavHostController

    @Before
    fun setUp() {
        // Inizializza le dipendenze, ma NON il ViewModel
        fakeDataStore = FakeAppSettingsDataStore()
        mockNavController = mock(NavHostController::class.java)
    }

    @After
    fun tearDown() {
        // Pulizia, se necessaria
    }

    @Test
    fun `toggleSelectAllBackgrounds when deselecting all falls back to first selected`() = runTest {
        // 1. Arrange
        val initialSelection = setOf("background_02", "background_04", "background_00")
        fakeDataStore.saveSelectedBackgrounds(initialSelection)

        viewModel = PreferencesViewModel(navController = mockNavController, appSettingsDataStore = fakeDataStore)
        advanceUntilIdle()

        assertThat(viewModel.selectedBackgrounds.value).isEqualTo(initialSelection)

        // 2. Act
        viewModel.prepareForBackgroundSelection()
        viewModel.toggleSelectAllBackgrounds(selectAll = false)

        // 3. Assert
        val expectedFallback = "background_00"
        assertThat(viewModel.selectedBackgrounds.value).containsExactly(expectedFallback)
    }

    @Test
    fun `updateCardSelection modifies only temp state`() = runTest {
        // 1. Arrange: Crea uno stato iniziale VALIDO
        val minRequired = (3 * 4) / 2 // 6 carte per una griglia 3x4
        fakeDataStore.saveBoardDimensions(3, 4)
        val initialCards = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveSelectedCards(initialCards)

        viewModel = PreferencesViewModel(navController = mockNavController, appSettingsDataStore = fakeDataStore)
        advanceUntilIdle()
        viewModel.prepareForCardSelection() // Popola lo stato temporaneo

        // Verifica che l'init non abbia sovrascritto i dati
        assertThat(viewModel.selectedCards.value).isEqualTo(initialCards)
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(initialCards)
        
        // 2. Act: Aggiungi una carta
        val newCard = "img_c_99"
        viewModel.updateCardSelection(newCard, isSelected = true)

        // 3. Assert: Verifica che solo lo stato temporaneo sia cambiato
        val expectedTempCards = initialCards + newCard
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(expectedTempCards)
        
        // Verifica che il DataStore (e il flow collegato) NON sia stato modificato
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(initialCards)
    }

    @Test
    fun `confirmCardSelections when selection is valid saves to DataStore`() = runTest {
        // 1. Arrange
        val minRequired = (3 * 4) / 2 // 6
        fakeDataStore.saveBoardDimensions(3, 4)
        fakeDataStore.saveSelectedCards(setOf("img_c_01")) // Stato iniziale volutamente non valido

        viewModel = PreferencesViewModel(navController = mockNavController, appSettingsDataStore = fakeDataStore)
        advanceUntilIdle()
        viewModel.prepareForCardSelection()

        // Pulisci lo stato temporaneo (che era stato inquinato dai valori di default)
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), selectAll = false)
        assertThat(viewModel.tempSelectedCards.value).isEmpty()

        val validSelection = (1..minRequired).map { "img_c_%02d".format(it) }.toSet()
        validSelection.forEach { viewModel.updateCardSelection(it, true) } // Crea una selezione valida
        assertThat(viewModel.tempSelectedCards.value).hasSize(minRequired)

        // 2. Act
        viewModel.confirmCardSelections()
        advanceUntilIdle() // Attendi il completamento della coroutine di salvataggio

        // 3. Assert
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(validSelection)
        assertThat(viewModel.cardSelectionError.value).isNull()
    }

    @Test
    fun `confirmCardSelections when selection is invalid does not save and sets error`() = runTest {
        // 1. Arrange
        val minRequired = (4 * 5) / 2 // 10
        val initialCards = (1..minRequired + 2).map { "img_c_%02d".format(it) }.toSet()
        fakeDataStore.saveBoardDimensions(4, 5)
        fakeDataStore.saveSelectedCards(initialCards)

        viewModel = PreferencesViewModel(navController = mockNavController, appSettingsDataStore = fakeDataStore)
        advanceUntilIdle()
        viewModel.prepareForCardSelection()

        val invalidSelection = setOf("img_s_01", "img_s_02") // Solo 2 carte
        viewModel.toggleSelectAllCards(viewModel.tempSelectedCards.value.toList(), false) // Deseleziona tutto
        invalidSelection.forEach { viewModel.updateCardSelection(it, true) } // Seleziona le 2 carte
        assertThat(viewModel.tempSelectedCards.value).isEqualTo(invalidSelection)

        // 2. Act
        viewModel.confirmCardSelections()
        advanceUntilIdle()

        // 3. Assert
        assertThat(viewModel.cardSelectionError.value).isNotNull()
        assertThat(fakeDataStore.selectedCards.value).isEqualTo(initialCards) // Deve rimanere lo stato iniziale
    }
}