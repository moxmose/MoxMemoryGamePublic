package com.example.moxmemorygame.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R
import com.example.moxmemorygame.model.ScoreEntry
import com.example.moxmemorygame.ui.OpeningMenuViewModel
import com.example.moxmemorygame.ui.composables.AboutDialog
import com.example.moxmemorygame.ui.composables.BackgroundImg
import org.koin.androidx.compose.koinViewModel

@Composable
fun OpeningMenuScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    openingMenuViewModel: OpeningMenuViewModel = koinViewModel()
) {
    val topRanking by openingMenuViewModel.topRanking.collectAsState()
    val lastPlayed by openingMenuViewModel.lastPlayedEntry.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(onDismissRequest = { showAboutDialog = false })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg(
            selectedBackgrounds = openingMenuViewModel.selectedBackgrounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.clickable { showAboutDialog = true },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 0.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.opening_menu_title),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LastGameAndRanking(
                topRanking = topRanking,
                lastPlayed = lastPlayed,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons at the bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Reduced padding to make buttons wider
            ) {
                Button(
                    onClick = { openingMenuViewModel.onStartGameClicked() },
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.opening_menu_button_start_game),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                OutlinedButton(
                    onClick = { openingMenuViewModel.onSettingsClicked() },
                    shape = RoundedCornerShape(topStart = 1.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.opening_menu_button_settings),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun LastGameAndRanking(
    topRanking: List<ScoreEntry>,
    lastPlayed: ScoreEntry?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Added vertical padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (topRanking.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.opening_menu_top_ranking),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Expands to fill available space
                    .testTag("RankingList"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(topRanking) { index, score ->
                    ScoreCard(
                        entry = score,
                        rank = index + 1,
                        isLastPlayed = score == lastPlayed
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer between list and last game
        }

        if (lastPlayed != null) {
            Text(
                text = stringResource(id = R.string.opening_menu_last_game_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ScoreCard(entry = lastPlayed, isLastPlayed = true, modifier = Modifier.testTag("LastPlayedCard"))
        }
    }
}

@Composable
fun ScoreCard(
    entry: ScoreEntry,
    modifier: Modifier = Modifier,
    rank: Int? = null,
    isLastPlayed: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLastPlayed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (rank != null) {
                    Text(
                        text = stringResource(id = R.string.rank_number_format, rank),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                Text(
                    text = entry.playerName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.weight(1f)) // This is the fix
            Text(
                text = stringResource(id = R.string.score_points_format, entry.score),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
