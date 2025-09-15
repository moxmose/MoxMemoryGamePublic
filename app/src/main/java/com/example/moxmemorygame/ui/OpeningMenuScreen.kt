package com.example.moxmemorygame.ui

import android.annotation.SuppressLint
// Rimosso Image, painterResource e R perché BackgroundImg condiviso dovrebbe gestirli
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
// Rimosso collectAsState e getValue se currentBackground non è più usato
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Rimosso ContentScale se BackgroundImg condiviso lo gestisce
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.BackgroundImg // Importa il Composable BackgroundImg condiviso
import com.example.moxmemorygame.IAppSettingsDataStore // Necessario per la Preview
//import com.example.moxmemorygame.FakeAppSettingsDataStoreUpdatedForBackgroundsAndCards // Necessario per la Preview
import org.koin.androidx.compose.koinViewModel

@Composable
fun OpeningMenuScreen(
    viewModel: OpeningMenuViewModel = koinViewModel(),
    innerPadding: PaddingValues
) {
    // val currentBackground by viewModel.backgroundPreference.collectAsState() // Non più necessario se BackgroundImg prende lo StateFlow

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        // Usa il Composable BackgroundImg condiviso, passando lo StateFlow dal ViewModel
        // Se il BackgroundImg condiviso non accetta alpha, l'alpha di 0.5f precedente andrà perso.
        // Potrebbe essere necessario modificare BackgroundImg per supportare alpha o wrapparlo.
        BackgroundImg(selectedBackgrounds = viewModel.backgroundPreference, alpha = 0.5f) // Tentiamo di passare alpha
        
        Column(
            modifier = Modifier
                // .padding(innerPadding) // Il padding è già applicato al Box esterno
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // Centra i bottoni e il testo
        ) {
            Text(
                text = "MOX MEMORY GAME",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp) // Aggiunto padding per spaziatura
            )
            Button(
                onClick = viewModel::onStartGameClicked,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 1.dp,
                    bottomStart = 1.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Modificato padding per coerenza
                    .fillMaxWidth(0.8f) // Rende i bottoni un po' meno larghi
            ) {
                Text(text = "START NEW GAME")
            }
            Button(
                onClick = viewModel::onSettingsClicked,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 1.dp,
                    bottomStart = 1.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Modificato padding per coerenza
                    .fillMaxWidth(0.8f) // Rende i bottoni un po' meno larghi
            ) {
                Text(text = "SETTINGS")
            }
        }
    }
}

// La funzione BackgroundImg locale è stata rimossa.

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun OpeningMenuScreenPreview() {
    // Utilizza una fake implementation di IAppSettingsDataStore per la preview
    val fakeAppSettingsDataStore = FakeAppSettingsDataStoreUpdatedForBackgroundsAndCards()
    val fakeViewModel = OpeningMenuViewModel(
        navController = rememberNavController(),
        appSettingsDataStore = fakeAppSettingsDataStore 
    )
    val fakePadding = PaddingValues(0.dp) // Usa 0.dp per la preview o un valore realistico
    OpeningMenuScreen(fakeViewModel, fakePadding)
}
