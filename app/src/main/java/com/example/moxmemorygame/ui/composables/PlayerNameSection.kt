package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R
import com.example.moxmemorygame.ui.PreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerNameSection(
    tempPlayerName: String,
    onPlayerNameChange: (String) -> Unit,
    onSavePlayerName: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Correct spacing
    ) {
        OutlinedTextField(
            value = tempPlayerName,
            onValueChange = { if (it.length <= PreferencesViewModel.PLAYERNAME_MAX_LENGTH) onPlayerNameChange(it) },
            label = { Text(stringResource(R.string.preferences_player_name_label, tempPlayerName.length, PreferencesViewModel.PLAYERNAME_MAX_LENGTH)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSavePlayerName,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 1.dp, bottomStart = 1.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.preferences_button_save_player_name), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
