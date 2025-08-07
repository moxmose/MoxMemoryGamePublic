package com.example.moxmemorygame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    preferencesViewModel: PreferencesViewModel = koinViewModel(), // Ottieni il ViewModel, magari tramite Koin o passandolo
    innerPadding: PaddingValues
) {
    val playerName by preferencesViewModel.playerName.collectAsState()
    val currentCardSet by preferencesViewModel.cardSet.collectAsState()
    val currentBackgroundPref by preferencesViewModel.backgroundPreference.collectAsState()

    // Stati locali per i TextField, se vuoi aggiornare solo al "Salva"
    var tempPlayerName by remember(playerName) { mutableStateOf(playerName) }
    // Per i set di carte e sfondi, potresti usare RadioButton o DropdownMenu

    val cardSets = listOf("Set 1 (Animali)", "Set 2 (Mostri)") // Esempio
    val backgroundOptions = listOf("Casuale", "Sfondo Fisso 1", "Sfondo Fisso 2") // Esempio

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding), // Apply innerPadding here
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg()
        Column(
            modifier = Modifier
//                .fillMaxSize()
                .padding(innerPadding)
                .fillMaxWidth()
//                .padding(16.dp),
                    ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("PREFERENCES",
                //style = MaterialTheme.typography.headlineSmall
                )

            OutlinedTextField(
                value = tempPlayerName,
                onValueChange = { if (it.length <= PreferencesViewModel.PLAYERNAME_MAX_LENGTH) tempPlayerName = it },
                label = { Text("PLAYER'S NAME [${tempPlayerName.length}/${PreferencesViewModel.PLAYERNAME_MAX_LENGTH}]") },
                singleLine = true,
            )

            // Selezione Set di Carte (esempio con RadioButton)
            Text("CARD SET:")
            cardSets.forEach { setOption ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (currentCardSet == setOption.split(" ")[0].lowercase()), // Semplice logica di matching
                        onClick = { preferencesViewModel.updateCardSet(setOption.split(" ")[0].lowercase()) }
                    )
                    Text(text = setOption, modifier = Modifier.padding(start = 8.dp))
                }
            }

            // Selezione Sfondo (esempio con RadioButton)
            Text("BACKGROUND:")
            backgroundOptions.forEach { bgOption ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (currentBackgroundPref == bgOption.replace(" ", "_")
                            .lowercase()), // Semplice logica
                        onClick = {
                            preferencesViewModel.updateBackgroundPreference(
                                bgOption.replace(
                                    " ",
                                    "_"
                                ).lowercase()
                            )
                        }
                    )
                    Text(text = bgOption, modifier = Modifier.padding(start = 8.dp))
                }
            }


            Button(onClick = {
                preferencesViewModel.updatePlayerName(tempPlayerName)
                // Le altre preferenze potrebbero essere già state aggiornate tramite RadioButton
                // Oppure, se usi stati temporanei anche per loro, salvale qui.
                // Potresti voler tornare alla schermata precedente o mostrare un messaggio di conferma
            },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 1.dp,
                    bottomStart = 1.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("SAVE PREFERENCES")
            }
        }
    }
}