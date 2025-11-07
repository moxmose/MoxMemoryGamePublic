package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R

@Composable
fun CardSelectionSection(
    modifier: Modifier = Modifier, // Added modifier
    selectedRefinedCount: Int,
    refinedCardResourceNames: List<String>,
    selectedSimpleCount: Int,
    simpleCardResourceNames: List<String>,
    minRequiredPairs: Int,
    selectedCardsCount: Int,
    onRefinedClick: () -> Unit,
    onSimpleClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(), // Applied modifier
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.preferences_card_selection_title), 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        OutlinedButton(
            onClick = onRefinedClick,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("RefinedCardsButton")
        ) {
            Text(stringResource(R.string.preferences_button_select_refined_cards, selectedRefinedCount, refinedCardResourceNames.size), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        }

        OutlinedButton(
            onClick = onSimpleClick,
            shape = RoundedCornerShape(topStart = 1.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("SimpleCardsButton")
        ) {
            Text(stringResource(R.string.preferences_button_select_simple_cards, selectedSimpleCount, simpleCardResourceNames.size), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        }

        val isSelectionInvalid = selectedCardsCount < minRequiredPairs

        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.preferences_min_cards_required_info_part1))
                append(" ")
                val part2 = stringResource(R.string.preferences_min_cards_required_info_part2, selectedCardsCount, minRequiredPairs)
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}
