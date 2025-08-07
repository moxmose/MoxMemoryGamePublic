package com.example.moxmemorygame

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.moxmemorygame.ui.GameCard
import com.example.moxmemorygame.ui.GameCardArray
import com.example.moxmemorygame.ui.GameCardImages
import com.example.moxmemorygame.ui.SoundUtils
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.BOARD_WIDTH
import com.example.moxmemorygame.ui.BOARD_HEIGHT
import com.example.moxmemorygame.ui.formatDuration
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameApp(
//    gameViewModel: GameViewModel = getViewModel(),
    gameViewModel: GameViewModel = koinViewModel(),
//    navController: NavHostController,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues
) {
    // vals to enable sounds
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
//    val actionOnResetProceed = { appViewModel.resetProceed(); resetSound() }
//    val actionOnResetProceed = { appViewModel.resetProceed(); appViewModel.setPlayResetSound() }
//    val actionOnResetProceed = { gameViewModel.onResetAndGoToOpeningMenu(); gameViewModel; gameViewModel.setPlayResetSound() }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding), // Apply innerPadding here
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg()
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

/**
 * Show points and statistics
 */
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
                text = "Score : $score",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Moves: $moves",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Row {
            Text(
                text = "Time: $timeGame",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Show the table - all the magic is delegated to Compose
 */
@Composable
fun ShowTablePlay(
    xDim: Int,
    yDim: Int,
    tablePlay: GameCardArray,
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
                        contentDescription = ""
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier.weight(1f))
    }
}

/**
 * Tail shows buttons to pause and reset
 */
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
            Text("RESET")
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
            Text("PAUSE")
        }
    }
}

/**
 * Set background
 */
@Composable
fun BackgroundImg(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.background_00),
        contentDescription = null,
        alpha = 0.5f,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Pause Dialog
 */
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
        // Draw a rectangle shape with rounded corners inside the dialog

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp)
                .clickable { onDismissRequest() }
            ,
            shape = //RoundedCornerShape(16.dp),
            RoundedCornerShape(
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
                    contentDescription = "PAUSE",
                    contentScale = ContentScale.Fit,
                    //modifier = Modifier
                    //    .height(160.dp)
                )
                Text(
                    text = "CLICK TO EXIT PAUSE",
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
        // Draw a rectangle shape with rounded corners inside the dialog

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(493.dp)
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
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.card_reset),
                    contentDescription = "PAUSE",
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "RESET GAME",
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = "ARE YOU SURE?",
                    modifier = Modifier.padding(0.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { onDismissRequest() },
                        shape = RoundedCornerShape(
                            topStart = 1.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 1.dp
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BACK")
                    }
                    Spacer( modifier = Modifier.padding(5.dp))
                    Button(
                        onClick = { onConfirmation() },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 1.dp,
                            bottomStart = 1.dp,
                            bottomEnd = 16.dp
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RESET")
                    }
                }
            }
        }
    }
}

/**
 * Pause Dialog
 */
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
        // Draw a rectangle shape with rounded corners inside the dialog

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(466.dp)
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
                    painter = painterResource(R.drawable.card_win),
                    contentDescription = "COMPLETED",
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "CONGRATULATION!",
                    modifier = Modifier.padding(16.dp),
                )
                Text(
                    text = "FINAL SCORE: $score",
                    fontWeight = FontWeight.Bold,
                    //modifier = Modifier.padding(16.dp),
                )
                Text(
                    text = "CLICK TO PLAY AGAIN",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
@Composable
fun Testing(
//    navigateToDetail: (Long) -> Unit,
    tablePlay: GameCardArray,
    //tablePlay: Array<Array<MutableState<GameCard>>>,
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
                        //setPlayCardTurned(0, 0, true)
                        setPlayCardTurned(0, 1, !(tablePlay.cardsArray[0][1].value.turned))
                    }
                )

            }
            // Text(text = "${tablePlay.cardsArray[0][0].turned.toString()}")
            Text(text = tablePlay.cardsArray[0][1].value.turned.toString())
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = if (tablePlay.cardsArray[0][0].value.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable {
                        //setPlayCardTurned(0, 0, true)
                        setPlayCardTurned(0, 0, !(tablePlay.cardsArray[0][0].value.turned))
                    }
                )

            }
           // Text(text = "${tablePlay.cardsArray[0][0].turned.toString()}")
            Text(text = tablePlay.cardsArray[0][0].value.turned.toString())
        }
    }
}



@Composable
fun TestingDelayUsingList(
//    navigateToDetail: (Long) -> Unit,
    tablePlay: GameCardArray,
    //tablePlay: Array<Array<MutableState<GameCard>>>,
    setPlayCardTurned: (Int, Int, Boolean) -> Unit,
    testList: SnapshotStateList<GameCard>,
    testValue: MutableState<GameCard>,
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
                        //setPlayCardTurned(0, 0, true)
                        testListFun(1)
                    }
                )

            }
            // Text(text = "${tablePlay.cardsArray[0][0].turned.toString()}")
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
            // Text(text = "${tablePlay.cardsArray[0][0].turned.toString()}")
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
        val tablePlay = GameCardArray()
        ShowTablePlay(
            xDim = BOARD_WIDTH,
            yDim = 4,//Y_BOARD_DIM,
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
