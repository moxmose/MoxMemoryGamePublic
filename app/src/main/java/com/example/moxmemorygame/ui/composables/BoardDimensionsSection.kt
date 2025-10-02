package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R
import com.example.moxmemorygame.ui.PreferencesViewModel
import kotlin.math.roundToInt

@Composable
fun BoardDimensionsSection(
    modifier: Modifier = Modifier, // Aggiunto modifier
    tempSliderWidth: Float,
    tempSliderHeight: Float,
    currentBoardWidth: Int,
    currentBoardHeight: Int,
    boardDimensionError: String?,
    onWidthChange: (Float) -> Unit,
    onHeightChange: (Float) -> Unit,
    onWidthChangeFinished: () -> Unit,
    onHeightChangeFinished: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(), // Applicato modifier
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.preferences_board_dimensions_title), 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Width: ${tempSliderWidth.roundToInt()}", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = tempSliderWidth,
                onValueChange = onWidthChange,
                valueRange = PreferencesViewModel.MIN_BOARD_WIDTH.toFloat()..PreferencesViewModel.MAX_BOARD_WIDTH.toFloat(),
                steps = (PreferencesViewModel.MAX_BOARD_WIDTH - PreferencesViewModel.MIN_BOARD_WIDTH - 1),
                onValueChangeFinished = onWidthChangeFinished,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Height: ${tempSliderHeight.roundToInt()}", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = tempSliderHeight,
                onValueChange = onHeightChange,
                valueRange = PreferencesViewModel.MIN_BOARD_HEIGHT.toFloat()..PreferencesViewModel.MAX_BOARD_HEIGHT.toFloat(),
                steps = (PreferencesViewModel.MAX_BOARD_HEIGHT - PreferencesViewModel.MIN_BOARD_HEIGHT - 1),
                onValueChangeFinished = onHeightChangeFinished,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "Current Size: ${currentBoardWidth}x${currentBoardHeight}", 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        boardDimensionError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
