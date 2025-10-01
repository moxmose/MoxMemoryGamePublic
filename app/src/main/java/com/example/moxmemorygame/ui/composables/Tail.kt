package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R

@Composable
fun Tail(
    actionOnPause: () -> Unit,
    actionOnReset: () -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
    ) {
        FilledTonalButton(
            onClick = actionOnReset,
            shape = RoundedCornerShape(
                topStart = 1.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 1.dp
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.game_tail_button_reset), style = MaterialTheme.typography.bodyLarge)
        }
        Spacer( modifier = Modifier.padding(5.dp))
        Button (
            onClick = actionOnPause,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 1.dp,
                bottomStart = 1.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.game_tail_button_pause), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TailPreview() {
    MaterialTheme {
        Tail(actionOnPause = {}, actionOnReset = {})
    }
}
