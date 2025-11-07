package com.example.moxmemorygame.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.R
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.model.BackgroundMusic
import com.example.moxmemorygame.ui.BackgroundMusicManager
import com.example.moxmemorygame.ui.PreferencesViewModel
import com.example.moxmemorygame.ui.composables.BackgroundImg
import com.example.moxmemorygame.ui.composables.BackgroundSelectionDialog
import com.example.moxmemorygame.ui.composables.BoardDimensionsSection
import com.example.moxmemorygame.ui.composables.CardSelectionDialog
import com.example.moxmemorygame.ui.composables.CardSelectionSection
import com.example.moxmemorygame.ui.composables.MusicSelectionDialog
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

    val tempSelectedCards by preferencesViewModel.tempSelectedCards.collectAsState()
    val selectedCardsFromDataStore by preferencesViewModel.selectedCards.collectAsState()

    val availableCardResourceNames = preferencesViewModel.availableCardResourceNames

    val currentBoardWidth by preferencesViewModel.selectedBoardWidth.collectAsState()
    val currentBoardHeight by preferencesViewModel.selectedBoardHeight.collectAsState()
    val boardDimensionError by preferencesViewModel.boardDimensionError.collectAsState()

    // Music states
    val isMusicEnabled by preferencesViewModel.isMusicEnabled.collectAsState()
    val musicVolume by preferencesViewModel.musicVolume.collectAsState()
    val selectedMusicTrackNames by preferencesViewModel.selectedMusicTrackNames.collectAsState()

    // SFX states
    val areSfxEnabled by preferencesViewModel.areSoundEffectsEnabled.collectAsState()
    val sfxVolume by preferencesViewModel.soundEffectsVolume.collectAsState()

    var tempPlayerName by remember(playerName) { mutableStateOf(playerName) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showRefinedCardDialog by remember { mutableStateOf(false) }
    var showSimpleCardDialog by remember { mutableStateOf(false) }
    var showMusicDialog by remember { mutableStateOf(false) }

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

    val refinedCardResourceNames = remember(availableCardResourceNames) {
        availableCardResourceNames.filter { it.startsWith("img_c_") }
    }
    val simpleCardResourceNames = remember(availableCardResourceNames) {
        availableCardResourceNames.filter { it.startsWith("img_s_") }
    }

    val refinedCountFromDataStore = selectedCardsFromDataStore.count { it.startsWith("img_c_") }
    val simpleCountFromDataStore = selectedCardsFromDataStore.count { it.startsWith("img_s_") }

    val tempRefinedCount = tempSelectedCards.count { it.startsWith("img_c_") }
    val tempSimpleCount = tempSelectedCards.count { it.startsWith("img_s_") }

    val minRequiredPairs = remember(currentBoardWidth, currentBoardHeight) {
        (currentBoardWidth * currentBoardHeight) / 2
    }

    // --- DIALOGS ---
    if (showBackgroundDialog) {
        BackgroundSelectionDialog(
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
    }

    if (showRefinedCardDialog) {
        CardSelectionDialog(
            onDismiss = { showRefinedCardDialog = false },
            onConfirm = {
                preferencesViewModel.confirmCardSelections()
                showRefinedCardDialog = false
            },
            cardResourceNames = refinedCardResourceNames,
            selectedCards = tempSelectedCards,
            onCardSelectionChanged = { cardName, isSelected ->
                preferencesViewModel.updateCardSelection(cardName, isSelected)
            },
            onToggleSelectAll = { selectAll ->
                preferencesViewModel.toggleSelectAllCards(refinedCardResourceNames, selectAll)
            },
            minRequired = minRequiredPairs,
            title = stringResource(R.string.preferences_button_select_refined_cards, tempRefinedCount, refinedCardResourceNames.size)
        )
    }

    if (showSimpleCardDialog) {
        CardSelectionDialog(
            onDismiss = { showSimpleCardDialog = false },
            onConfirm = {
                preferencesViewModel.confirmCardSelections()
                showSimpleCardDialog = false
            },
            cardResourceNames = simpleCardResourceNames,
            selectedCards = tempSelectedCards,
            onCardSelectionChanged = { cardName, isSelected ->
                preferencesViewModel.updateCardSelection(cardName, isSelected)
            },
            onToggleSelectAll = { selectAll ->
                preferencesViewModel.toggleSelectAllCards(simpleCardResourceNames, selectAll)
            },
            minRequired = minRequiredPairs,
            title = stringResource(R.string.preferences_button_select_simple_cards, tempSimpleCount, simpleCardResourceNames.size)
        )
    }

    if (showMusicDialog) {
        MusicSelectionDialog(
            onDismiss = { 
                preferencesViewModel.stopMusicPreview()
                showMusicDialog = false 
            },
            onConfirm = { trackNames ->
                preferencesViewModel.saveSelectedMusicTracks(trackNames)
                showMusicDialog = false
            },
            onPlayPreview = { track -> preferencesViewModel.playMusicPreview(track) },
            allTracks = BackgroundMusic.allTracks,
            initialSelection = selectedMusicTrackNames
        )
        DisposableEffect(Unit) {
            onDispose {
                preferencesViewModel.stopMusicPreview()
            }
        }
    }

    // --- MAIN UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        BackgroundImg(selectedBackgrounds = preferencesViewModel.selectedBackgrounds, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .testTag("PreferencesList") // Added for testing
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
                                preferencesViewModel.prepareForBackgroundSelection()
                                showBackgroundDialog = true
                            },
                            shape = RoundedCornerShape(topStart = 1.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 1.dp),
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
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                    }

                    item {
                        MusicPreferencesSection(
                            isMusicEnabled = isMusicEnabled,
                            onMusicEnabledChange = { preferencesViewModel.saveIsMusicEnabled(it) },
                            musicVolume = musicVolume,
                            onMusicVolumeChange = { preferencesViewModel.saveMusicVolume(it) },
                            selectedTracksCount = selectedMusicTrackNames.size,
                            onSelectTracksClicked = { showMusicDialog = true }
                        )
                    }
                    
                    item {
                        SoundEffectsPreferencesSection(
                            areSfxEnabled = areSfxEnabled,
                            onSfxEnabledChange = { preferencesViewModel.saveAreSoundEffectsEnabled(it) },
                            sfxVolume = sfxVolume,
                            onSfxVolumeChange = { preferencesViewModel.saveSoundEffectsVolume(it) }
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

@Composable
fun MusicPreferencesSection(
    isMusicEnabled: Boolean,
    onMusicEnabledChange: (Boolean) -> Unit,
    musicVolume: Float,
    onMusicVolumeChange: (Float) -> Unit,
    selectedTracksCount: Int,
    onSelectTracksClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preferences_music_settings_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.preferences_music_enable_label), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isMusicEnabled, 
                onCheckedChange = onMusicEnabledChange,
                modifier = Modifier.testTag("MusicSwitch")
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.preferences_music_volume_label, (musicVolume * 100).roundToInt()), style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = musicVolume,
                onValueChange = onMusicVolumeChange,
                valueRange = 0f..1f,
                enabled = isMusicEnabled,
                modifier = Modifier.testTag("MusicVolumeSlider")
            )
        }

        OutlinedButton(
            onClick = onSelectTracksClicked,
            enabled = isMusicEnabled,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.preferences_button_select_music_tracks, selectedTracksCount), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SoundEffectsPreferencesSection(
    areSfxEnabled: Boolean,
    onSfxEnabledChange: (Boolean) -> Unit,
    sfxVolume: Float,
    onSfxVolumeChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preferences_sfx_settings_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.preferences_sfx_enable_label), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = areSfxEnabled, 
                onCheckedChange = onSfxEnabledChange,
                modifier = Modifier.testTag("SfxSwitch")
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.preferences_sfx_volume_label, (sfxVolume * 100).roundToInt()), style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = sfxVolume,
                onValueChange = onSfxVolumeChange,
                valueRange = 0f..1f,
                enabled = areSfxEnabled,
                modifier = Modifier.testTag("SfxVolumeSlider")
            )
        }
    }
}


@SuppressLint("ComposableViewModelCreation", "UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun PreferencesScreenPreview() {
    val fakeDataStore = FakeAppSettingsDataStore()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fakeDataStore.savePlayerName("Preview Player")
        fakeDataStore.saveSelectedBackgrounds(setOf("background_01", "background_02"))
    }

    val fakeMusicManager = BackgroundMusicManager(
        context = context,
        appSettingsDataStore = fakeDataStore,
        externalScope = scope
    )

    val fakeViewModel = PreferencesViewModel(
        navController = rememberNavController(),
        appSettingsDataStore = fakeDataStore,
        backgroundMusicManager = fakeMusicManager
    )

    MaterialTheme {
        PreferencesScreen(
            preferencesViewModel = fakeViewModel,
            innerPadding = PaddingValues(0.dp)
        )
    }
}
