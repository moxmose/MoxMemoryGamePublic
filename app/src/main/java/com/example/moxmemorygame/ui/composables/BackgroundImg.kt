package com.example.moxmemorygame.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.moxmemorygame.R
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BackgroundImg(
    selectedBackgrounds: StateFlow<Set<String>>,
    modifier: Modifier = Modifier, 
    alpha: Float = 0.5f
) {
    val context = LocalContext.current
    val currentSelectedSet by selectedBackgrounds.collectAsState()

    val backgroundNameToDisplay = remember(currentSelectedSet) {
        if (currentSelectedSet.isNotEmpty()) {
            currentSelectedSet.randomOrNull() ?: "background_00"
        } else {
            "background_00"
        }
    }

    val drawableId = remember(backgroundNameToDisplay, context) {
        try {
            context.resources.getIdentifier(
                backgroundNameToDisplay,
                "drawable",
                context.packageName
            )
        } catch (_: Exception) { 
            R.drawable.background_00
        }
    }

    if (drawableId != 0) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null, 
            alpha = alpha,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.background_00),
            contentDescription = stringResource(R.string.game_background_default_error_description),
            alpha = alpha,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
