package com.example.moxmemorygame.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background // Added for ImagePreviewDialog
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.BackgroundImg
// Import aggiornati
import com.example.moxmemorygame.data.local.IAppSettingsDataStore // Aggiornato
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore // Per la Preview
// RealAppSettingsDataStore non è importato direttamente qui perché non serve più dopo lo spostamento della classe Fake
import com.example.moxmemorygame.model.BOARD_HEIGHT
import com.example.moxmemorygame.model.BOARD_WIDTH
import com.example.moxmemorygame.model.ScoreEntry 
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    preferencesViewModel: PreferencesViewModel = koinViewModel(),
    innerPadding: PaddingValues
) {
    val playerName by preferencesViewModel.playerName.collectAsState()
    val selectedBackgroundsFromVM by preferencesViewModel.selectedBackgrounds.collectAsState()
    val availableBackgrounds = preferencesViewModel.availableBackgrounds

    val selectedCardsFromDataStore by preferencesViewModel.appSettingsDataStore.selectedCards.collectAsState()
    val availableCardResourceNames = preferencesViewModel.availableCardResourceNames

    var tempPlayerName by remember(playerName) { mutableStateOf(playerName) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showRefinedCardDialog by remember { mutableStateOf(false) }
    var showSimpleCardDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cardSelectionError by preferencesViewModel.cardSelectionError.collectAsState()

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

    val selectedRefinedCount = remember(selectedCardsFromDataStore) {
        selectedCardsFromDataStore.count { it.startsWith("img_c_") }
    }
    val selectedSimpleCount = remember(selectedCardsFromDataStore) {
        selectedCardsFromDataStore.count { it.startsWith("img_s_") }
    }
    val minRequiredCards = (BOARD_WIDTH * BOARD_HEIGHT) / 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        BackgroundImg(selectedBackgrounds = preferencesViewModel.selectedBackgrounds) 
        Column(modifier = Modifier.fillMaxSize()) { 
            LazyColumn(
                modifier = Modifier
                    .weight(1f) 
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text("PREFERENCES", style = MaterialTheme.typography.headlineMedium)
                }

                item {
                    OutlinedTextField(
                        value = tempPlayerName,
                        onValueChange = { if (it.length <= PreferencesViewModel.PLAYERNAME_MAX_LENGTH) tempPlayerName = it },
                        label = { Text("PLAYER'S NAME [${tempPlayerName.length}/${PreferencesViewModel.PLAYERNAME_MAX_LENGTH}]") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Button(
                        onClick = {
                            preferencesViewModel.updatePlayerName(tempPlayerName)
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE PLAYER NAME")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showBackgroundDialog = true },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SELECT BACKGROUNDS\n(${selectedBackgroundsFromVM.size} selected)", textAlign = TextAlign.Center)
                    }
                }

                item {
                    Text("CARD SELECTION:", style = MaterialTheme. typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                }

                item {
                    OutlinedButton(
                        onClick = { showRefinedCardDialog = true },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SELECT REFINED CARDS\n($selectedRefinedCount/${refinedCardResourceNames.size} selected)", textAlign = TextAlign.Center)
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showSimpleCardDialog = true },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("SELECT SIMPLE CARDS\n($selectedSimpleCount/${simpleCardResourceNames.size} selected)", textAlign = TextAlign.Center)
                    }
                }
                item {
                    Text(
                        "Minimum $minRequiredCards cards required in total (currently ${selectedCardsFromDataStore.size}).",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { preferencesViewModel.onBackToMainMenuClicked() },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BACK TO MAIN MENU")
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (showBackgroundDialog) {
            BackgroundSelectionDialog(
                availableBackgrounds = availableBackgrounds,
                initialSelectedBackgrounds = selectedBackgroundsFromVM,
                selectedBackgroundsFlow = preferencesViewModel.selectedBackgrounds, 
                onDismissRequest = { showBackgroundDialog = false },
                onConfirm = { confirmedSelection ->
                    preferencesViewModel.confirmBackgroundSelections(confirmedSelection)
                    showBackgroundDialog = false
                }
            )
        }

        if (showRefinedCardDialog) {
            CardSetSelectionDialog(
                cardTypeDisplayName = "Refined",
                availableCardsForThisSet = refinedCardResourceNames,
                initiallySelectedCardsInThisSet = selectedCardsFromDataStore.filter { it.startsWith("img_c_") }.toSet(),
                getCardDisplayName = preferencesViewModel::getCardDisplayName,
                selectedBackgroundsFlow = preferencesViewModel.selectedBackgrounds, 
                onDismissRequest = { showRefinedCardDialog = false },
                onConfirm = { confirmedSelectionForSet ->
                    preferencesViewModel.confirmCardSelectionsForSet(confirmedSelectionForSet, "img_c_")
                    showRefinedCardDialog = false
                }
            )
        }

        if (showSimpleCardDialog) {
            CardSetSelectionDialog(
                cardTypeDisplayName = "Simple",
                availableCardsForThisSet = simpleCardResourceNames,
                initiallySelectedCardsInThisSet = selectedCardsFromDataStore.filter { it.startsWith("img_s_") }.toSet(),
                getCardDisplayName = preferencesViewModel::getCardDisplayName,
                selectedBackgroundsFlow = preferencesViewModel.selectedBackgrounds,
                onDismissRequest = { showSimpleCardDialog = false },
                onConfirm = { confirmedSelectionForSet ->
                    preferencesViewModel.confirmCardSelectionsForSet(confirmedSelectionForSet, "img_s_")
                    showSimpleCardDialog = false
                }
            )
        }
    }
}


@Composable
fun CardSetSelectionDialog(
    cardTypeDisplayName: String,
    availableCardsForThisSet: List<String>,
    initiallySelectedCardsInThisSet: Set<String>,
    getCardDisplayName: (String) -> String,
    selectedBackgroundsFlow: StateFlow<Set<String>>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var tempSelectedCards by remember { mutableStateOf(initiallySelectedCardsInThisSet) }
    var previewedImageName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(initiallySelectedCardsInThisSet) {
        tempSelectedCards = initiallySelectedCardsInThisSet
    }

    val toggleCardSelection = { cardName: String ->
        val current = tempSelectedCards.toMutableSet()
        if (current.contains(cardName)) {
            current.remove(cardName)
        } else {
            current.add(cardName)
        }
        tempSelectedCards = current
    }

    val toggleSelectAllForThisSet = {
        if (tempSelectedCards.size == availableCardsForThisSet.size) {
            tempSelectedCards = emptySet()
        } else {
            tempSelectedCards = availableCardsForThisSet.toSet()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box {
                BackgroundImg(
                    selectedBackgrounds = selectedBackgroundsFlow,
                    modifier = Modifier.matchParentSize(),
                    alpha = 0.15f
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Select $cardTypeDisplayName Cards",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "(Click image to preview)",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { toggleSelectAllForThisSet() }
                            ) {
                                Checkbox(
                                    checked = tempSelectedCards.size == availableCardsForThisSet.size && availableCardsForThisSet.isNotEmpty(),
                                    onCheckedChange = { toggleSelectAllForThisSet() }
                                )
                                Text(text = "Select/Deselect All (${availableCardsForThisSet.size})")
                            }
                        }
                        items(availableCardsForThisSet) { cardName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { toggleCardSelection(cardName) }
                            ) {
                                Checkbox(
                                    checked = tempSelectedCards.contains(cardName),
                                    onCheckedChange = { toggleCardSelection(cardName) }
                                )
                                Spacer(Modifier.width(8.dp))
                                val drawableId = remember(cardName) {
                                    try { context.resources.getIdentifier(cardName, "drawable", context.packageName) } catch (e: Exception) { 0 }
                                }
                                if (drawableId != 0) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = "Miniatura ${getCardDisplayName(cardName)}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(40.dp).padding(end = 8.dp).clickable { previewedImageName = cardName }
                                    )
                                } else {
                                    Spacer(Modifier.size(40.dp).padding(end = 8.dp))
                                }
                                Text(
                                    text = getCardDisplayName(cardName),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            shape = RoundedCornerShape(topStart = 1.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { onConfirm(tempSelectedCards) },
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OK", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (previewedImageName != null) {
        ImagePreviewDialog(
            imageName = previewedImageName!!,
            onDismissRequest = { previewedImageName = null }
        )
    }
}


@Composable
fun BackgroundSelectionDialog(
    availableBackgrounds: List<String>,
    initialSelectedBackgrounds: Set<String>,
    selectedBackgroundsFlow: StateFlow<Set<String>>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var tempSelectedBgs by remember { mutableStateOf(initialSelectedBackgrounds) }
    var previewedImageName by remember { mutableStateOf<String?>(null) } 
    val context = LocalContext.current

    LaunchedEffect(initialSelectedBackgrounds) {
        tempSelectedBgs = initialSelectedBackgrounds
    }

    val toggleSelection = { bgName: String ->
        val current = tempSelectedBgs.toMutableSet()
        if (current.contains(bgName)) {
            if (current.size > 1) { 
                current.remove(bgName)
            }
        } else {
            current.add(bgName)
        }
        tempSelectedBgs = current
    }

    val toggleSelectAll = {
        if (tempSelectedBgs.size == availableBackgrounds.size) {
            if (availableBackgrounds.isNotEmpty()){
                 tempSelectedBgs = setOf(availableBackgrounds.first())
            } else {
                tempSelectedBgs = emptySet()
            }
        } else {
            tempSelectedBgs = availableBackgrounds.toSet()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 1.dp,
                bottomStart = 1.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box {
                BackgroundImg(
                    selectedBackgrounds = selectedBackgroundsFlow,
                    modifier = Modifier.matchParentSize(),
                    alpha = 0.15f
                )
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Select Backgrounds",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "(Click image to preview)",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toggleSelectAll() }
                            ) {
                                Checkbox(
                                    checked = tempSelectedBgs.size == availableBackgrounds.size,
                                    onCheckedChange = { toggleSelectAll() }
                                )
                                Text(text = "Select/Deselect All", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        items(availableBackgrounds) { bgName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toggleSelection(bgName) } 
                            ) {
                                Checkbox(
                                    checked = tempSelectedBgs.contains(bgName),
                                    onCheckedChange = { toggleSelection(bgName) } 
                                )
                                Spacer(Modifier.width(8.dp))
                                val drawableId = remember(bgName) { 
                                    try {
                                        context.resources.getIdentifier(bgName, "drawable", context.packageName)
                                    } catch (e: Exception) {
                                        0 
                                    }
                                }
                                if (drawableId != 0) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = "Miniatura $bgName",
                                        contentScale = ContentScale.Crop, 
                                        modifier = Modifier
                                            .size(40.dp) 
                                            .padding(end = 8.dp) 
                                            .clickable { previewedImageName = bgName } 
                                    )
                                } else {
                                    Spacer(Modifier.size(40.dp).padding(end=8.dp))
                                }
                                Text(
                                    text = bgName.replace("_", " ").replaceFirstChar { 
                                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                                    },
                                    modifier = Modifier.weight(1f) 
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            shape = RoundedCornerShape(topStart = 1.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { onConfirm(tempSelectedBgs) },
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OK", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (previewedImageName != null) {
        ImagePreviewDialog(
            imageName = previewedImageName!!, 
            onDismissRequest = { previewedImageName = null }
        )
    }
}


@Composable
fun ImagePreviewDialog(
    imageName: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f) 
                .background(Color.Black.copy(alpha = 0.75f)) 
                .clickable(onClick = onDismissRequest), 
            contentAlignment = Alignment.Center
        ) {
            val drawableId = remember(imageName) {
                try {
                    context.resources.getIdentifier(imageName, "drawable", context.packageName)
                } catch (e: Exception) {
                    0 
                }
            }

            if (drawableId != 0) {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = "Anteprima $imageName",
                    contentScale = ContentScale.Fit, 
                    modifier = Modifier
                        .padding(16.dp) 
                        .fillMaxWidth() 
                )
            } else {
                Text(
                    "Immagine non trovata",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreferencesScreenPreview() {
    val navController = rememberNavController()
    // Utilizza la classe FakeAppSettingsDataStore dal nuovo package e con il nome semplificato
    val fakeAppSettingsDataStore = FakeAppSettingsDataStore() 
    val fakeViewModel = PreferencesViewModel(navController, fakeAppSettingsDataStore)
    PreferencesScreen(
        preferencesViewModel = fakeViewModel,
        innerPadding = PaddingValues(0.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun BackgroundSelectionDialogPreview() {
    val availableBackgrounds = List(7) { i -> "background_%02d".format(i) }
    val initialSelected = setOf(availableBackgrounds[0], availableBackgrounds[2])
    val selectedFlow = MutableStateFlow(initialSelected).asStateFlow()

    MaterialTheme { 
        BackgroundSelectionDialog(
            availableBackgrounds = availableBackgrounds,
            initialSelectedBackgrounds = initialSelected,
            selectedBackgroundsFlow = selectedFlow,
            onDismissRequest = { },
            onConfirm = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) 
@Composable
fun CardSetSelectionDialogPreview() {
    MaterialTheme {
        val available = (0..5).map { "img_c_%02d".format(it) }
        val initial = setOf(available[0], available[2])
        val selectedBgsFlow = MutableStateFlow(setOf("background_00"))
        CardSetSelectionDialog(
            cardTypeDisplayName = "Refined Preview",
            availableCardsForThisSet = available,
            initiallySelectedCardsInThisSet = initial,
            getCardDisplayName = { rn -> if (rn.startsWith("img_c_")) "Refined ${rn.removePrefix("img_c_")}" else rn },
            selectedBackgroundsFlow = selectedBgsFlow,
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) 
@Composable
fun ImagePreviewDialogPreview() {
    MaterialTheme {
        val imageName = "background_01"
        ImagePreviewDialog(
            imageName = imageName,
            onDismissRequest = {}
        )
    }
}

// La definizione di FakeAppSettingsDataStoreUpdatedForBackgroundsAndCards è stata rimossa da qui
// e spostata in data/local/FakeAppSettingsDataStore.kt
