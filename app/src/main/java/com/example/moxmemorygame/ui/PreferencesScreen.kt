package com.example.moxmemorygame.ui

import android.annotation.SuppressLint
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
// Rimosso TextButton perché ora usiamo Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.BackgroundImg
import com.example.moxmemorygame.IAppSettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    preferencesViewModel: PreferencesViewModel = koinViewModel(),
    innerPadding: PaddingValues
) {
    val playerName by preferencesViewModel.playerName.collectAsState()
    val currentCardSet by preferencesViewModel.cardSet.collectAsState()
    val selectedBackgroundsFromVM by preferencesViewModel.selectedBackgrounds.collectAsState()
    val availableBackgrounds = preferencesViewModel.availableBackgrounds

    var tempPlayerName by remember(playerName) { mutableStateOf(playerName) }
    var showBackgroundDialog by remember { mutableStateOf(false) }

    val cardSets = listOf("Set 1 (Animali)", "Set 2 (Mostri)")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg(selectedBackgrounds = preferencesViewModel.selectedBackgrounds)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text("PREFERENCES")
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
                Text("CARD SET:")
            }
            items(cardSets) { setOption ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { preferencesViewModel.updateCardSet(setOption.split(" ")[0].lowercase()) }
                ) {
                    RadioButton(
                        selected = (currentCardSet == setOption.split(" ")[0].lowercase()),
                        onClick = { preferencesViewModel.updateCardSet(setOption.split(" ")[0].lowercase()) }
                    )
                    Text(text = setOption, modifier = Modifier.padding(start = 8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false), // Per non farla espandere troppo se il contenuto è poco
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
                                Text(
                                    text = bgName.replace("_", " ").replaceFirstChar { 
                                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center // Centra la Row dei pulsanti
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            shape = RoundedCornerShape(
                                topStart = 1.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 1.dp
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", style = MaterialTheme.typography.labelMedium) // Dimensione font ridotta
                        }
                        Spacer(modifier = Modifier.width(10.dp)) // Spazio tra i pulsanti
                        Button(
                            onClick = { onConfirm(tempSelectedBgs) },
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 1.dp,
                                bottomStart = 1.dp,
                                bottomEnd = 16.dp
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OK", style = MaterialTheme.typography.labelMedium) // Dimensione font ridotta
                        }
                    }
                }
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreferencesScreenPreview() {
    val navController = rememberNavController()
    val fakeAppSettingsDataStore = FakeAppSettingsDataStoreUpdatedForBackgroundsAndSets()
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

    // Theme wrapper for MaterialTheme
    MaterialTheme {
        BackgroundSelectionDialog(
            availableBackgrounds = availableBackgrounds,
            initialSelectedBackgrounds = initialSelected,
            selectedBackgroundsFlow = selectedFlow,
            onDismissRequest = { /* Azione Preview Annulla */ },
            onConfirm = { /* Azione Preview Conferma: it -> contiene il set selezionato */ }
        )
    }
}

class FakeAppSettingsDataStoreUpdatedForBackgroundsAndSets : IAppSettingsDataStore {
    override val playerName: StateFlow<String> = MutableStateFlow("Test Player")
    override val cardSet: StateFlow<String> = MutableStateFlow("set1") 
    override val selectedBackgrounds: StateFlow<Set<String>> = MutableStateFlow(setOf("background_00", "background_01"))

    override suspend fun savePlayerName(name: String) {}
    override suspend fun saveCardSet(newSet: String) {}
    override suspend fun saveSelectedBackgrounds(backgrounds: Set<String>) {
        // (this._selectedBackgrounds as MutableStateFlow<Set<String>>).value = backgrounds
    }
}