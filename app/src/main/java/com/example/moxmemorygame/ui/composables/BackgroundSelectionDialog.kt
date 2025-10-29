package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.moxmemorygame.R
import java.util.Locale

@Composable
fun BackgroundSelectionDialog(
    onDismiss: () -> Unit,
    availableBackgrounds: List<String>,
    selectedBackgrounds: Set<String>,
    onBackgroundSelectionChanged: (String, Boolean) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        BackgroundSelectionDialogContent(
            onDismiss = onDismiss,
            availableBackgrounds = availableBackgrounds,
            selectedBackgrounds = selectedBackgrounds,
            onBackgroundSelectionChanged = onBackgroundSelectionChanged,
            onToggleSelectAll = onToggleSelectAll
        )
    }
}

@Composable
private fun BackgroundSelectionDialogContent(
    onDismiss: () -> Unit,
    availableBackgrounds: List<String>,
    selectedBackgrounds: Set<String>,
    onBackgroundSelectionChanged: (String, Boolean) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit
) {
    var selectedImageForPreview by remember { mutableStateOf<String?>(null) }

    if (selectedImageForPreview != null) {
        ImagePreviewDialog(
            imageName = selectedImageForPreview!!,
            onDismiss = { selectedImageForPreview = null }
        )
    } else {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 0.dp,
                bottomStart = 0.dp,
                bottomEnd = 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.background_selection_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val allSelected = remember(availableBackgrounds, selectedBackgrounds) {
                    availableBackgrounds.isNotEmpty() && selectedBackgrounds.containsAll(availableBackgrounds)
                }
                OutlinedButton(
                    onClick = { onToggleSelectAll(!allSelected) },
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_select_deselect_all),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Box(modifier = Modifier.weight(1f, fill = false)) {
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(availableBackgrounds) { bgName ->
                            val context = LocalContext.current
                            val drawableId = remember(bgName) {
                                context.resources.getIdentifier(bgName, "drawable", context.packageName)
                            }

                            val isChecked by remember(selectedBackgrounds) { mutableStateOf(bgName in selectedBackgrounds) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val isCurrentlySelected = selectedBackgrounds.contains(bgName)
                                            if (!(isCurrentlySelected && selectedBackgrounds.size == 1)) {
                                                onBackgroundSelectionChanged(bgName, !isCurrentlySelected)
                                            }
                                        }
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { 
                                            val isCurrentlySelected = selectedBackgrounds.contains(bgName)
                                            if (!(isCurrentlySelected && selectedBackgrounds.size == 1)) {
                                                onBackgroundSelectionChanged(bgName, it) 
                                            }
                                        }
                                    )
                                    Text(
                                        text = formatResourceName(bgName),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                if (drawableId != 0) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = stringResource(R.string.dialog_item_thumbnail_description, bgName),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable { selectedImageForPreview = bgName }
                                    )
                                }
                            }
                        }
                    }

                    if (lazyListState.canScrollForward) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.preferences_scroll_down_indicator),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 16.dp
                    )
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

private fun formatResourceName(name: String): String {
    return name
        .replace("_", " ")
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}


@Preview(showBackground = true)
@Composable
fun BackgroundSelectionDialogPreview() {
    val bgList = List(5) { "background_%02d".format(it) } 
    var selected by remember { mutableStateOf(setOf("background_00")) }

    MaterialTheme {
        BackgroundSelectionDialogContent(
            onDismiss = {},
            availableBackgrounds = bgList,
            selectedBackgrounds = selected,
            onBackgroundSelectionChanged = { bgName, isSelected ->
                if (isSelected) {
                    selected = selected + bgName
                } else {
                    if (selected.size > 1) {
                        selected = selected - bgName
                    }
                }
            },
            onToggleSelectAll = { selectAll ->
                selected = if (selectAll) {
                    bgList.toSet()
                } else {
                    val fallback = bgList.firstOrNull { it in selected } ?: bgList.first()
                    setOf(fallback)
                }
            }
        )
    }
}
