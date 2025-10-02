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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Import per stringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.ui.composables.BackgroundImg
// Import per la classe R
import com.example.moxmemorygame.R
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.ScoreEntry
import com.example.moxmemorygame.ui.OpeningMenuViewModel
import org.koin.androidx.compose.koinViewModel

private const val MAX_VISIBLE_RANKING_ENTRIES = 5

@Composable
fun OpeningMenuScreen(
    viewModel: OpeningMenuViewModel = koinViewModel(),
    innerPadding: PaddingValues // Questo padding viene dal Scaffold principale
) {
    val topRanking by viewModel.topRanking.collectAsState()
    val lastPlayedEntry by viewModel.lastPlayedEntry.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg(selectedBackgrounds = viewModel.backgroundPreference, modifier = Modifier.fillMaxSize(), alpha = 0.5f) // MODIFICATO

        Column(modifier = Modifier.fillMaxSize()) { // Colonna principale per layout
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Occupa lo spazio disponibile sopra i pulsanti
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top // Allineamento in alto per il contenuto della LazyColumn
            ) {
                // Item 1: Titolo
                item {
                    Text(
                        text = stringResource(R.string.opening_menu_title),
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 24.dp) // Spazio sopra e sotto il titolo
                    )
                }

                // Item 2: Top Ranking
                if (topRanking.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.opening_menu_top_ranking),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }
                    itemsIndexed(topRanking.take(MAX_VISIBLE_RANKING_ENTRIES)) { index, entry ->
                        val isCurrentEntryHighlighted = entry.timestamp == lastPlayedEntry?.timestamp && entry.score == lastPlayedEntry?.score && entry.playerName == lastPlayedEntry?.playerName
                        RankingEntryView(
                            rank = index + 1,
                            entry = entry,
                            isHighlighted = isCurrentEntryHighlighted
                        )
                        if (index < topRanking.take(MAX_VISIBLE_RANKING_ENTRIES).lastIndex) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp)) // Spazio dopo il ranking
                    }
                } else {
                    item { 
                        // Spacer opzionale se il ranking è vuoto e si vuole mantenere uno spazio
                        // Spacer(modifier = Modifier.height(MaterialTheme.typography.headlineSmall.fontSize.value.dp * 2)) // Esempio
                    }
                }

                // Item 3: Last Played
                item {
                    lastPlayedEntry?.let {
                        LastPlayedEntryView(entry = it) // Ora userà lo stile di evidenziazione
                        Spacer(modifier = Modifier.height(16.dp)) 
                    }
                }
            } // Fine LazyColumn per contenuto scrollabile

            // Sezione Pulsanti (fissa in basso, fuori dalla LazyColumn)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp) 
                    .padding(top = 16.dp, bottom = 24.dp), 
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom // Assicura che i pulsanti stiano in basso nella loro colonna dedicata
            ) {
                Button(
                    onClick = viewModel::onStartGameClicked,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 1.dp,
                        bottomStart = 1.dp,
                        bottomEnd = 16.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = stringResource(R.string.opening_menu_button_start_game), style = MaterialTheme.typography.bodyLarge)
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
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = stringResource(R.string.opening_menu_button_settings), style = MaterialTheme.typography.bodyLarge)
                }
            }
        } // Fine Colonna principale
    }
}

@Composable
fun LastPlayedEntryView(entry: ScoreEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors( // Stile unificato con l'highlight del ranking
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) { // Padding generale della Card
            // Titolo della Card
            Text(
                text = stringResource(R.string.opening_menu_last_game_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer, // Colore testo per coerenza con highlight
                modifier = Modifier.fillMaxWidth(), 
                textAlign = TextAlign.Center 
            )
            Spacer(modifier = Modifier.height(8.dp)) 

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically 
            ) {
                Column(modifier = Modifier.weight(0.7f)) { 
                    Text(
                        text = entry.playerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold, 
                        color = MaterialTheme.colorScheme.onSecondaryContainer // Colore testo
                    )
                    Text(
                        text = entry.dateTime,
                        fontSize = 10.sp, 
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) // Colore testo
                    )
                }
                Text(
                    text = stringResource(R.string.score_points_format, entry.score), // MODIFICATO: Punteggio visualizzato direttamente
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold, 
                    textAlign = TextAlign.End, 
                    color = MaterialTheme.colorScheme.onSecondaryContainer, // Colore testo
                    modifier = Modifier.weight(0.3f) 
                )
            }
        }
    }
}

@Composable
fun RankingEntryView(rank: Int, entry: ScoreEntry, isHighlighted: Boolean) {
    val cardColors = if (isHighlighted) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    }
    val textColor = if (isHighlighted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(6.dp),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.rank_number_format, rank),
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                modifier = Modifier.weight(0.15f)
            )
            Column(modifier = Modifier.weight(0.55f)) {
                Text(
                    text = entry.playerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = entry.dateTime,
                    fontSize = 10.sp, 
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            Text(
                text = stringResource(R.string.score_points_format, entry.score), // MODIFICATO: Punteggio visualizzato direttamente
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                color = textColor,
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun OpeningMenuScreenPreview() {
    val fakeDataStore = FakeAppSettingsDataStore()
    LaunchedEffect(Unit) {
        // I punteggi salvati per la preview sono ora intesi come valori diretti
        // 1234 sarà visualizzato come 1234
        fakeDataStore.saveScore("UserPreview", 1234) 
        fakeDataStore.saveScore("GiocatoreEstremo", 3200) 
        fakeDataStore.saveScore("PlayerNumeroUno", 2505) 
        fakeDataStore.saveScore("MoxFan", 1800)     
        fakeDataStore.saveScore("AnotherPlayer", 1509) 
        fakeDataStore.saveScore("YetAnother", 1000) 
        fakeDataStore.saveScore("HighScorerLAST", 2802) 
    }

    val fakeViewModel = OpeningMenuViewModel(
        navController = rememberNavController(),
        appSettingsDataStore = fakeDataStore
    )
    MaterialTheme {
        OpeningMenuScreen(fakeViewModel, PaddingValues(0.dp))
    }
}

@Preview
@Composable
fun LastPlayedEntryPreview() {
    // entry.score = 1250 visualizzerà 1250
    val entry = ScoreEntry("Preview Player", 1250, System.currentTimeMillis() - 100000000)
    MaterialTheme {
        Box(modifier=Modifier.padding(16.dp)) {
            LastPlayedEntryView(entry = entry)
        }
    }
}

@Preview
@Composable
fun RankingEntryPreview() {
    // entry.score = 2800 visualizzerà 2800
    val entry = ScoreEntry("Rank Preview", 2800, System.currentTimeMillis() - 200000000)
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)){
            RankingEntryView(rank = 1, entry = entry, isHighlighted = false)
            // entry.score = 2753 visualizzerà 2753
            RankingEntryView(rank = 2, entry = entry.copy(playerName = "Highlighted Player", score = 2753), isHighlighted = true)
        }
    }
}
