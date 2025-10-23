package com.example.moxmemorygame.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.R
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.ui.PreferencesViewModel
import com.example.moxmemorygame.ui.composables.BackgroundImg
import com.example.moxmemorygame.ui.composables.BackgroundSelectionDialog
import com.example.moxmemorygame.ui.composables.BoardDimensionsSection
import com.example.moxmemorygame.ui.composables.CardSelectionDialog
import com.example.moxmemorygame.ui.composables.CardSelectionSection
import com.example.moxmemorygame.ui.composables.PlayerNameSection
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    preferencesViewModel: PreferencesViewModel = koinViewModel(),
    innerPadding: PaddingValues
) {
    val playerName by preferencesViewModel.playerName.collectAsState()
    val selectedBackgroundsFromVM by preferencesViewModel.selectedBackgrounds.collectAsState()
    val availableBackgrounds = preferencesViewModel.availableBackgrounds

    // Use temp state for the dialog UI
    val tempSelectedCards by preferencesViewModel.tempSelectedCards.collectAsState()
    val selectedCardsFromDataStore by preferencesViewModel.selectedCards.collectAsState()

    val availableCardResourceNames = preferencesViewModel.availableCardResourceNames

    val currentBoardWidth by preferencesViewModel.selectedBoardWidth.collectAsState()
    val currentBoardHeight by preferencesViewModel.selectedBoardHeight.collectAsState()
    val boardDimensionError by preferencesViewModel.boardDimensionError.collectAsState()

    var tempPlayerName by remember(playerName) { mutableStateOf(playerName) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showRefinedCardDialog by remember { mutableStateOf(false) }
    var showSimpleCardDialog by remember { mutableStateOf(false) }

    var tempSliderWidth by remember(currentBoardWidth) { mutableStateOf(currentBoardWidth.toFloat()) }
    var tempSliderHeight by remember(currentBoardHeight) { mutableStateOf(currentBoardHeight.toFloat()) }

    LaunchedEffect(currentBoardWidth) {
        tempSliderWidth = currentBoardWidth.toFloat()
    }
    LaunchedEffect(currentBoardHeight) {
        tempSliderHeight = currentBoardHeight.toFloat()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cardSelectionError by preferencesViewModel.cardSelectionError.collectAsState()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(cardSelectionError) {
        cardSelectionError?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = androidx.compose.material3.SnackbarDuration.Short
                )
                preferencesViewModel.clearCardSelectionError()
            }
        }
    }

    LaunchedEffect(boardDimensionError) {
        boardDimensionError?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = androidx.compose.material3.SnackbarDuration.Long
                )
                preferencesViewModel.clearBoardDimensionError()
            }
        }
    }

    val refinedCardResourceNames = remember(availableCardResourceNames) {
        availableCardResourceNames.filter { it.startsWith("img_c_") }
    }
    val simpleCardResourceNames = remember(availableCardResourceNames) {
        availableCardResourceNames.filter { it.startsWith("img_s_") }
    }

    // Counts for the main UI (based on the saved state)
    val refinedCountFromDataStore = selectedCardsFromDataStore.count { it.startsWith("img_c_") }
    val simpleCountFromDataStore = selectedCardsFromDataStore.count { it.startsWith("img_s_") }

    // Counts for the dialogs (based on the temporary and reactive state)
    val tempRefinedCount = tempSelectedCards.count { it.startsWith("img_c_") }
    val tempSimpleCount = tempSelectedCards.count { it.startsWith("img_s_") }

    val minRequiredPairs = remember(currentBoardWidth, currentBoardHeight) {
        (currentBoardWidth * currentBoardHeight) / 2
    }

    BackgroundSelectionDialog(
        showDialog = showBackgroundDialog,
        onDismiss = { showBackgroundDialog = false },
        availableBackgrounds = availableBackgrounds,
        selectedBackgrounds = selectedBackgroundsFromVM,
        onBackgroundSelectionChanged = { bgName, isSelected ->
            preferencesViewModel.updateBackgroundSelection(bgName, isSelected)
        },
        onToggleSelectAll = { selectAll ->
            preferencesViewModel.toggleSelectAllBackgrounds(selectAll)
        }
    )

    CardSelectionDialog(
        showDialog = showRefinedCardDialog,
        onDismiss = { showRefinedCardDialog = false },
        onConfirm = {
            preferencesViewModel.confirmCardSelections()
            showRefinedCardDialog = false
        },
        cardResourceNames = refinedCardResourceNames,
        selectedCards = tempSelectedCards, // Use temp state
        onCardSelectionChanged = { cardName, isSelected ->
            preferencesViewModel.updateCardSelection(cardName, isSelected)
        },
        onToggleSelectAll = { selectAll ->
            preferencesViewModel.toggleSelectAllCards(refinedCardResourceNames, selectAll)
        },
        minRequired = minRequiredPairs,
        title = stringResource(R.string.preferences_button_select_refined_cards, tempRefinedCount, refinedCardResourceNames.size)
    )

    CardSelectionDialog(
        showDialog = showSimpleCardDialog,
        onDismiss = { showSimpleCardDialog = false },
        onConfirm = {
            preferencesViewModel.confirmCardSelections()
            showSimpleCardDialog = false
        },
        cardResourceNames = simpleCardResourceNames,
        selectedCards = tempSelectedCards, // Use temp state
        onCardSelectionChanged = { cardName, isSelected ->
            preferencesViewModel.updateCardSelection(cardName, isSelected)
        },
        onToggleSelectAll = { selectAll ->
            preferencesViewModel.toggleSelectAllCards(simpleCardResourceNames, selectAll)
        },
        minRequired = minRequiredPairs,
        title = stringResource(R.string.preferences_button_select_simple_cards, tempSimpleCount, simpleCardResourceNames.size)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        BackgroundImg(selectedBackgrounds = preferencesViewModel.selectedBackgrounds, modifier = Modifier.fillMaxSize()) // MODIFIED
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Text(stringResource(R.string.preferences_screen_title), style = MaterialTheme.typography.headlineMedium)
                    }

                    item {
                        PlayerNameSection(
                            tempPlayerName = tempPlayerName,
                            onPlayerNameChange = { tempPlayerName = it },
                            onSavePlayerName = { preferencesViewModel.updatePlayerName(tempPlayerName) }
                        )
                    }

                    item {
                        OutlinedButton(

                            onClick = {
                                // 1. FIRST prepare the fallback in the ViewModel
                                preferencesViewModel.prepareForBackgroundSelection()

                                // 2. THEN show the dialog
                                showBackgroundDialog = true
                            },
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.preferences_button_select_backgrounds, selectedBackgroundsFromVM.size), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    item {
                        BoardDimensionsSection(
                            modifier = Modifier.padding(top = 8.dp),
                            tempSliderWidth = tempSliderWidth,
                            tempSliderHeight = tempSliderHeight,
                            currentBoardWidth = currentBoardWidth,
                            currentBoardHeight = currentBoardHeight,
                            boardDimensionError = boardDimensionError,
                            onWidthChange = { tempSliderWidth = it },
                            onHeightChange = { tempSliderHeight = it },
                            onWidthChangeFinished = { preferencesViewModel.updateBoardDimensions(tempSliderWidth.roundToInt(), currentBoardHeight) },
                            onHeightChangeFinished = { preferencesViewModel.updateBoardDimensions(currentBoardWidth, tempSliderHeight.roundToInt()) }
                        )
                    }

                    item {
                        CardSelectionSection(
                            modifier = Modifier.padding(top = 8.dp),
                            selectedRefinedCount = refinedCountFromDataStore,
                            refinedCardResourceNames = refinedCardResourceNames,
                            selectedSimpleCount = simpleCountFromDataStore,
                            simpleCardResourceNames = simpleCardResourceNames,
                            minRequiredPairs = minRequiredPairs,
                            selectedCardsCount = selectedCardsFromDataStore.size,
                            onRefinedClick = {
                                preferencesViewModel.prepareForCardSelection()
                                showRefinedCardDialog = true
                            },
                            onSimpleClick = {
                                preferencesViewModel.prepareForCardSelection()
                                showSimpleCardDialog = true
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { preferencesViewModel.onBackToMainMenuClicked() },
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.preferences_button_back_to_main_menu), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                if (lazyListState.canScrollForward) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.preferences_scroll_down_indicator),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}


@SuppressLint("ComposableViewModelCreation", "UnrememberedMutableState",
    "ViewModelConstructorInComposable"
)
@Preview(showBackground = true)
@Composable
fun PreferencesScreenPreview() {
    // Fake ViewModel for preview
    val fakeDataStore = FakeAppSettingsDataStore()

    // Pre-populate the datastore for the preview
    LaunchedEffect(Unit) {
        fakeDataStore.savePlayerName("Preview Player")
        fakeDataStore.saveSelectedBackgrounds(setOf("background_01", "background_02"))
    }

    val fakeViewModel = PreferencesViewModel(
        navController = rememberNavController(),
        appSettingsDataStore = fakeDataStore
    )

    MaterialTheme {
        PreferencesScreen(
            preferencesViewModel = fakeViewModel,
            innerPadding = PaddingValues(0.dp)
        )
    }
}
