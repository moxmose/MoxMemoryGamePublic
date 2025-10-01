package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moxmemorygame.R

@Composable
fun GameWonDialogContent(
    onDismissRequest: () -> Unit,
    score: Int
) {
    val subtitleResId = if (score > 0) R.string.game_dialog_won_subtitle else R.string.game_dialog_finished_subtitle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 1.dp,
            bottomStart = 1.dp,
            bottomEnd = 16.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.card_win),
                contentDescription = stringResource(R.string.game_dialog_won_title),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.game_dialog_won_title),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = stringResource(subtitleResId),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            Text(
                text = stringResource(R.string.game_dialog_won_score_info, score),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onDismissRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(
                    topStart = 1.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 1.dp
                )
            ) {
                Text(stringResource(R.string.game_dialog_won_button_main_menu), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun GameWonDialog(
    onDismissRequest: () -> Unit,
    score: Int
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        GameWonDialogContent(onDismissRequest = onDismissRequest, score = score)
    }
}

@Preview(showBackground = true)
@Composable
fun GameWonDialogPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GameWonDialogContent(onDismissRequest = {}, score = 1500)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameFinishedDialogPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GameWonDialogContent(onDismissRequest = {}, score = 0)
        }
    }
}
