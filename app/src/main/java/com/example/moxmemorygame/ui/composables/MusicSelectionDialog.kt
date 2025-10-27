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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onPlayPreview: (BackgroundMusic) -> Unit,
    allTracks: List<BackgroundMusic>,
    initialSelection: Set<String>
) {
    var currentSelection by remember { mutableStateOf(initialSelection) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            // Consistent shape with other dialogs
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 16.dp),
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
                        onConfirm(emptySet())
                        onDismiss()
                    },
                    // Consistent shape
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.preferences_music_selection_none))
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
                                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                                )
                                IconButton(onClick = { onPlayPreview(track) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.preferences_music_preview_button_description, track.displayName))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Removed "Cancel" button and made "OK" button full width
                Button(
                    onClick = { onConfirm(currentSelection) },
                    // Consistent shape
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        }
    }
}
