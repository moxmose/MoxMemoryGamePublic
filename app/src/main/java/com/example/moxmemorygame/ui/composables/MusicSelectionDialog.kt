package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.moxmemorygame.R
import com.example.moxmemorygame.model.BackgroundMusic

@Composable
fun MusicSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    allTracks: List<BackgroundMusic>,
    initialSelection: Set<String>
) {
    var currentSelection by remember { mutableStateOf(initialSelection) }
    val allTrackNames = remember { allTracks.map { it.trackName }.toSet() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.preferences_music_selection_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedButton(
                    onClick = {
                        if (currentSelection == allTrackNames) {
                            currentSelection = emptySet()
                        } else {
                            currentSelection = allTrackNames
                        }
                    },
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dialog_select_deselect_all),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allTracks) { track ->
                        if (track != BackgroundMusic.None) { // Don't show "None" as a selectable option
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newSelection = currentSelection.toMutableSet()
                                        if (newSelection.contains(track.trackName)) {
                                            newSelection.remove(track.trackName)
                                        } else {
                                            newSelection.add(track.trackName)
                                        }
                                        currentSelection = newSelection
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = currentSelection.contains(track.trackName),
                                    onCheckedChange = { isChecked ->
                                        val newSelection = currentSelection.toMutableSet()
                                        if (isChecked) {
                                            newSelection.add(track.trackName)
                                        } else {
                                            newSelection.remove(track.trackName)
                                        }
                                        currentSelection = newSelection
                                    }
                                )
                                Text(
                                    text = track.displayName,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirm(currentSelection) },
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.button_ok),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
