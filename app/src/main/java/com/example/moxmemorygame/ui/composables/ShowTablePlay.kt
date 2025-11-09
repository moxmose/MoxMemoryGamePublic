package com.example.moxmemorygame.ui.composables

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.moxmemorygame.R
import com.example.moxmemorygame.model.GameBoard

@Composable
fun ShowTablePlay(
    xDim: Int,
    yDim: Int,
    tablePlay: GameBoard, 
    @DrawableRes
    gameCardImages: List<Int>,
    checkPlayCardTurned: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (gameCardImages.isEmpty() && (xDim * yDim > 0)) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.game_error_images_not_loaded), color = MaterialTheme.colorScheme.error)
        }
        return
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .testTag("GameBoard") // <-- This is the fix
    ) {
        for (y in 0 until yDim) {
            Spacer(Modifier.weight(1f))
            Row(Modifier
                .fillMaxWidth()
                .weight(5f)) {
                Spacer(Modifier.weight(1f))
                for (x in 0 until xDim) {
                    val cardState = tablePlay.cardsArray.getOrNull(x)?.getOrNull(y)
                    if (cardState != null) {
                        val cardValue = cardState.value
                        if (cardValue != null) {
                            val turned = cardValue.turned
                            val cardImageId = if (cardValue.id >= 0 && cardValue.id < gameCardImages.size) {
                                gameCardImages[cardValue.id]
                            } else {
                                R.drawable.card_back 
                            }

                            Image(
                                painter = if (!turned)
                                    painterResource(id = R.drawable.card_back)
                                else painterResource(id = cardImageId),
                                modifier = Modifier
                                    .weight(5f)
                                    .testTag("Card_${x}_${y}")
                                    .clickable { checkPlayCardTurned(x, y) },
                                contentScale = if (!turned) ContentScale.FillBounds
                                else ContentScale.Crop,
                                contentDescription = if (!turned) stringResource(R.string.game_card_content_description_back) else stringResource(R.string.game_card_content_description_face)
                            )
                        } else {
                            Log.e("ShowTablePlay", "CRITICAL: cardState.value is NULL at x=$x, y=$y")
                            Spacer(Modifier.weight(5f)) 
                        }
                    } else {
                        Log.w("ShowTablePlay", "Attempted to access card at invalid or null coordinate: x=$x, y=$y")
                        Spacer(Modifier.weight(5f)) 
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier.weight(1f))
    }
}
