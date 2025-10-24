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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moxmemorygame.R

@Composable
fun CardSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    cardResourceNames: List<String>,
    selectedCards: Set<String>,
    onCardSelectionChanged: (String, Boolean) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit,
    minRequired: Int,
    title: String
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true)
    ) {
        CardSelectionDialogContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            cardResourceNames = cardResourceNames,
            selectedCards = selectedCards,
            onCardSelectionChanged = onCardSelectionChanged,
            onToggleSelectAll = onToggleSelectAll,
            minRequired = minRequired,
            title = title
        )
    }
}

@Composable
private fun CardSelectionDialogContent(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    cardResourceNames: List<String>,
    selectedCards: Set<String>,
    onCardSelectionChanged: (String, Boolean) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit,
    minRequired: Int,
    title: String
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                val isSelectionInvalid = selectedCards.size < minRequired
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.preferences_min_cards_required_info_part1))
                        append(" ")
                        val part2 = stringResource(R.string.preferences_min_cards_required_info_part2, selectedCards.size, minRequired)
                        if (isSelectionInvalid) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)) {
                                append(part2)
                            }
                        } else {
                            append(part2)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val allSelected = remember(cardResourceNames, selectedCards) {
                    cardResourceNames.isNotEmpty() && selectedCards.containsAll(cardResourceNames)
                }
                OutlinedButton(
                    onClick = { onToggleSelectAll(!allSelected) },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 16.dp
                    ),
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
                        items(cardResourceNames) { cardName ->
                            val context = LocalContext.current
                            val drawableId = remember(cardName) {
                                context.resources.getIdentifier(cardName, "drawable", context.packageName)
                            }

                            val displayName = when {
                                cardName.startsWith("img_c_") -> stringResource(R.string.card_name_refined, cardName.removePrefix("img_c_").removePrefix("0"))
                                cardName.startsWith("img_s_") -> stringResource(R.string.card_name_simple, cardName.removePrefix("img_s_").removePrefix("0"))
                                else -> cardName.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            }

                            var isChecked by remember(selectedCards) { mutableStateOf(cardName in selectedCards) }
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
                                            isChecked = !isChecked
                                            onCardSelectionChanged(cardName, isChecked)
                                        }
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { onCardSelectionChanged(cardName, it) }
                                    )
                                    Text(
                                        text = displayName,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                if (drawableId != 0) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = stringResource(R.string.dialog_item_thumbnail_description, cardName),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable { selectedImageForPreview = cardName }
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
                    onClick = onConfirm,
                    enabled = selectedCards.size >= minRequired, // Added enable check
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

@Composable
fun ImagePreviewDialog(imageName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val drawableId = remember(imageName) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .clickable { onDismiss() }
        ) {
            if (drawableId != 0) {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = stringResource(id = R.string.image_preview_dialog_content_description, imageName),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(stringResource(id = R.string.image_preview_dialog_image_not_found), modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardSelectionDialogPreview() {
    val cardList = List(20) { "img_c_%02d".format(it) }
    var selected by remember { mutableStateOf(setOf("img_c_00")) }

    MaterialTheme {
        CardSelectionDialogContent(
            onDismiss = {},
            onConfirm = {},
            cardResourceNames = cardList,
            selectedCards = selected,
            onCardSelectionChanged = { cardName, isSelected ->
                selected = if (isSelected) selected + cardName else selected - cardName
            },
            onToggleSelectAll = { selectAll ->
                selected = if (selectAll) selected + cardList.toSet() else selected - cardList.toSet()
            },
            minRequired = 10, // Example for the preview
            title = stringResource(id = R.string.preferences_button_select_refined_cards, 0, 0)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePreviewDialogPreview() {
    MaterialTheme {
        ImagePreviewDialog(imageName = "img_c_00", onDismiss = {})
    }
}
