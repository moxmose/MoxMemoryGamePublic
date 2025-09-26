package com.example.moxmemorygame

import android.content.Context
import android.os.Build
import android.view.SoundEffectConstants
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight // Import per wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
// Import per stringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moxmemorygame.model.BOARD_HEIGHT // Nuovo Import
import com.example.moxmemorygame.model.BOARD_WIDTH // Nuovo Import
import com.example.moxmemorygame.model.GameBoard // Nuovo Import
// Import per la classe R (se non già presente per altre risorse)
// import com.example.moxmemorygame.R
import com.example.moxmemorygame.ui.GameCardImages
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.SoundUtils
import com.example.moxmemorygame.ui.formatDuration
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

@Composable
fun GameApp(
    gameViewModel: GameViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues
) {
    val localSoundContext = LocalContext.current
    val clickSound = { SoundUtils.playSound(localSoundContext, SoundEffectConstants.CLICK) }
    val flipSound = { SoundUtils.playSound(localSoundContext, R.raw.flipcard) }
    val pauseSound = { SoundUtils.playSound(localSoundContext, R.raw.keyswipe_card) }
    val failSound = { SoundUtils.playSound(localSoundContext, R.raw.fail) }
    val resetSound = { SoundUtils.playSound(localSoundContext, R.raw.card_mixing) }
    val successSound = { SoundUtils.playSound(localSoundContext, R.raw.short_success_sound_glockenspiel_treasure_videogame) }
    val winSound = { SoundUtils.playSound(localSoundContext, R.raw.brass_fanfare_with_timpani_and_winchimes_reverberated) }

    val tablePlay = gameViewModel.tablePlay

    val checkPlayCardTurned = {x: Int, y: Int ->
        gameViewModel.checkGamePlayCardTurned(x=x, y=y,
            flipSound=flipSound, pauseSound=pauseSound, failSound=failSound,
            successSound=successSound, winSound=winSound) }

    val actionOnPause = { gameViewModel.setResetPause(); pauseSound() }
    val actionOnReset = { gameViewModel.setResetReset(); pauseSound() }
    val actionOnResetProceed = { gameViewModel.onResetAndGoToOpeningMenu(); }

    val gameCardImages = gameViewModel.gameCardImages
    val gamePaused = gameViewModel.gamePaused.value
    val gameResetRequest = gameViewModel.gameResetRequest.value
    val gameWon = gameViewModel.gameWon.value
    val gamePlayResetSound = gameViewModel.gamePlayResetSound
    val resetPlayResetSound = { gameViewModel.resetPlayResetSound(resetSound) }

    val score = gameViewModel.score.intValue
    val moves = gameViewModel.moves.intValue
    val timeGame by gameViewModel.currentTime.collectAsState()
    val timeGameString = timeGame.formatDuration()

    val selectedBackgrounds by gameViewModel.selectedBackgrounds.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg(selectedBackgrounds = gameViewModel.selectedBackgrounds) // Passa lo StateFlow
        Column(modifier = modifier) {
            Head(
                score = score,
                moves = moves,
                timeGame = timeGameString
            )

            if (gamePlayResetSound) {
                resetPlayResetSound()
            }

            if (gamePaused)
                if (gameWon)
                    GameWonDialog(
                        onDismissRequest = actionOnResetProceed,
                        score = score
                    )
                else
                    if (gameResetRequest)
                        ResetDialog(
                            onDismissRequest = actionOnReset,
                            onConfirmation = actionOnResetProceed
                        )
                    else
                        PauseDialog(
                            onDismissRequest = actionOnPause
                        )

            ShowTablePlay(
                xDim = BOARD_WIDTH,
                yDim = BOARD_HEIGHT,
                tablePlay = tablePlay,
                gameCardImages = gameCardImages,
                checkPlayCardTurned = checkPlayCardTurned,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

            Tail(
                actionOnPause = actionOnPause,
                actionOnReset = actionOnReset
            )
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }
}

@Composable
fun Head(
    score: Int,
    moves: Int,
    timeGame: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 5.dp, start = 10.dp, end = 10.dp)
    ) {
        Row {
            Text(
                text = stringResource(R.string.game_head_score, score),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.game_head_moves, moves),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Row {
            Text(
                text = stringResource(R.string.game_head_time, timeGame),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

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
    Column(
        modifier = modifier
            .fillMaxHeight()
    ) {
        for (y in 0 until yDim) {
            Spacer(Modifier.weight(1f))
            Row(Modifier
                .fillMaxWidth()
                .weight(5f)) {
                Spacer(Modifier.weight(1f))
                for (x in 0 until xDim) {
                    val turned = tablePlay.cardsArray[x][y].value.turned
                    Image(
                        painter = if (!turned)
                            painterResource(id = R.drawable.card_back)
                        else painterResource(
                            gameCardImages[
                                tablePlay.cardsArray[x][y].value.id
                            ]
                        ),
                        modifier = Modifier
                            .weight(5f)
                            .clickable { checkPlayCardTurned( x,y ) },
                        contentScale = if (!turned) ContentScale.FillBounds
                        else ContentScale.Crop,
                        contentDescription = if (!turned) stringResource(R.string.game_card_content_description_back) else stringResource(R.string.game_card_content_description_face)
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier.weight(1f))
    }
}

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

@Composable
fun BackgroundImg(
    selectedBackgrounds: StateFlow<Set<String>>,
    modifier: Modifier = Modifier.fillMaxSize(),
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
        } catch (e: Exception) {
            R.drawable.background_00
        }
    }

    if (drawableId != 0) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null, // Backgrounds are decorative
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


@Composable
fun PauseDialog(
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp)
                .clickable { onDismissRequest() }
            ,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 1.dp,
                bottomStart = 1.dp,
                bottomEnd = 16.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.card_pause),
                    contentDescription = stringResource(R.string.game_dialog_pause_image_description),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = stringResource(R.string.game_dialog_pause_exit_prompt),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun ResetDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties( 
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp), 
            shape = RoundedCornerShape(
                topStart = 1.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.card_reset),
                    contentDescription = stringResource(R.string.game_dialog_reset_image_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.game_dialog_reset_title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = stringResource(R.string.game_dialog_reset_confirmation_prompt),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 0.dp),
                )
                Spacer(modifier = Modifier.height(16.dp)) // MODIFICATO da 24.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp), 
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { onDismissRequest() },
                        shape = RoundedCornerShape(
                            topStart = 1.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.button_cancel), style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Button(
                        onClick = { onConfirmation() },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.button_ok), style = MaterialTheme.typography.bodyLarge)
                    }
                }
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
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
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.card_win),
                    contentDescription = stringResource(R.string.game_dialog_won_title),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(150.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.game_dialog_won_title),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.game_dialog_won_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(R.string.game_dialog_won_score_info, score),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.fillMaxWidth(),
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
}


@Preview(showBackground = true)
@Composable
fun HeadPreview() {
    MaterialTheme {
        Head(score = 123, moves = 45, timeGame = "01:23")
    }
}

@Preview(showBackground = true)
@Composable
fun TailPreview() {
    MaterialTheme {
        Tail(actionOnPause = {}, actionOnReset = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PauseDialogPreview() {
    MaterialTheme {
        PauseDialog (onDismissRequest = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ResetDialogPreview() {
    MaterialTheme {
        ResetDialog (onDismissRequest = {}, onConfirmation = {})
    }
}

@Preview(showBackground = true)
@Composable
fun GameWonDialogPreview() {
    MaterialTheme {
        GameWonDialog(onDismissRequest = {}, score = 1500)
    }
}

@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
@Composable
fun Testing(
    tablePlay: GameBoard, 
    setPlayCardTurned: (Int, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val screenDensity = configuration.densityDpi / 160f
    val screenHeightPx = (configuration.screenHeightDp.toFloat() * screenDensity).toInt()
    val screenWidthPx = (configuration.screenWidthDp.toFloat() * screenDensity).toInt()

    Box()
    {

        Image(
            painter = painterResource(id = R.drawable.background_00),
            contentDescription = null,
            alpha = 0.7f,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.size(width = 100.dp, height = 150.dp))
            Box(modifier = Modifier.size(width = 100.dp, height = 150.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.card_back),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

            }
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = if (tablePlay.cardsArray[0][1].value.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable {
                        setPlayCardTurned(0, 1, !(tablePlay.cardsArray[0][1].value.turned))
                    }
                )

            }
            Text(text = tablePlay.cardsArray[0][1].value.turned.toString())
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = if (tablePlay.cardsArray[0][0].value.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable {
                        setPlayCardTurned(0, 0, !(tablePlay.cardsArray[0][0].value.turned))
                    }
                )

            }
            Text(text = tablePlay.cardsArray[0][0].value.turned.toString())
        }
    }
}



@Composable
fun TestingDelayUsingList(
    tablePlay: GameBoard, 
    setPlayCardTurned: (Int, Int, Boolean) -> Unit,
    testList: SnapshotStateList<com.example.moxmemorygame.model.GameCard>, 
    testValue: MutableState<com.example.moxmemorygame.model.GameCard>,
    testListFun: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val screenDensity = configuration.densityDpi / 160f
    val screenHeightPx = (configuration.screenHeightDp.toFloat() * screenDensity).toInt()
    val screenWidthPx = (configuration.screenWidthDp.toFloat() * screenDensity).toInt()

    Box()
    {

        Image(
            painter = painterResource(id = R.drawable.background_00),
            contentDescription = null,
            alpha = 0.7f,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.size(width = 100.dp, height = 150.dp))
            Box(modifier = Modifier.size(width = 100.dp, height = 150.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.card_back),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

            }
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = if (testValue.value.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable {
                        testListFun(1)
                    }
                )

            }
            Text(text = tablePlay.cardsArray[0][1].value.turned.toString())
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = if (tablePlay.cardsArray[0][0].value.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable {
                        setPlayCardTurned(0, 0, !(tablePlay.cardsArray[0][0].value.turned))
                    }
                )

            }
            Text(text = "${tablePlay.cardsArray[0][0].value.turned}")
        }
    }
}

@Preview
@Composable
fun TestingPreview() {
    Column {
        Head(
            score = 0,
            moves = 0,
            timeGame = "05:39"
        )
        val gameCardImages = GameCardImages().image
        val tablePlay = GameBoard() 
        ShowTablePlay(
            xDim = BOARD_WIDTH,
            yDim = BOARD_HEIGHT, 
            tablePlay = tablePlay,
            gameCardImages = gameCardImages,
            checkPlayCardTurned = {x, y -> },
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        Tail(
            {},
            {}
        )
        GameWonDialog(
            {},
            1000
        )
    }
}
