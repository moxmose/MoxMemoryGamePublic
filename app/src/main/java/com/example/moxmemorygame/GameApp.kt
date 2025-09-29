package com.example.moxmemorygame

import android.content.Context
import android.os.Build
import android.util.Log 
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moxmemorygame.model.GameBoard
import com.example.moxmemorygame.model.GameCard 
import com.example.moxmemorygame.ui.GameCardImages
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.SoundUtils
import com.example.moxmemorygame.ui.formatDuration
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameApp(
    gameViewModel: GameViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues
) {
    val localSoundContext = LocalContext.current
    val flipSound = { SoundUtils.playSound(localSoundContext, R.raw.flipcard) }
    val pauseSound = { SoundUtils.playSound(localSoundContext, R.raw.keyswipe_card) }
    val failSound = { SoundUtils.playSound(localSoundContext, R.raw.fail) }
    val resetSound = { SoundUtils.playSound(localSoundContext, R.raw.card_mixing) }
    val successSound = { SoundUtils.playSound(localSoundContext, R.raw.short_success_sound_glockenspiel_treasure_videogame) }
    val winSound = { SoundUtils.playSound(localSoundContext, R.raw.brass_fanfare_with_timpani_and_winchimes_reverberated) }

    val currentTablePlay = gameViewModel.tablePlay 
    val isBoardInitialized by gameViewModel.isBoardInitialized

    val checkPlayCardTurned = {x: Int, y: Int ->
        gameViewModel.checkGamePlayCardTurned(x=x, y=y,
            flipSound=flipSound, pauseSound=pauseSound, failSound=failSound,
            successSound=successSound, winSound=winSound) }

    // Azioni per i pulsanti Pause e Reset nella UI (Tail)
    val onPauseClicked = { gameViewModel.requestPauseDialog(); pauseSound() }
    val onResetClicked = { gameViewModel.requestResetDialog(); pauseSound() } // Usa requestResetDialog che imposta entrambi gli stati

    // Azioni per i dialoghi
    val onDismissPauseDialog = { gameViewModel.dismissPauseDialog(); pauseSound() }
    val onCancelResetDialog = { gameViewModel.cancelResetDialog(); pauseSound() }
    // Azione per conferma reset (OK) e per chiudere GameWonDialog: naviga al menu principale
    val onConfirmAndNavigateToMenu = { gameViewModel.navigateToOpeningMenuAndCleanupDialogStates(); /* Suono di reset o vittoria già gestito? */ }
    // Azione per resettare la partita corrente (se si volesse un pulsante OK nel ResetDialog che fa solo questo)
    // val onConfirmResetCurrentGame = { gameViewModel.resetCurrentGame(); resetSound() }

    val gameCardImages = gameViewModel.gameCardImages 
    val gamePaused by gameViewModel.gamePaused // Usa by per osservare lo State
    val gameResetRequest by gameViewModel.gameResetRequest // Usa by per osservare lo State
    val gameWon by gameViewModel.gameWon // Usa by per osservare lo State
    val gamePlayResetSound = gameViewModel.gamePlayResetSound // Questo è un Boolean normale, non State?
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
        BackgroundImg(selectedBackgrounds = gameViewModel.selectedBackgrounds)
        Column(modifier = modifier) {
            Head(
                score = score, 
                moves = moves,
                timeGame = timeGameString
            )

            if (gamePlayResetSound) {
                resetPlayResetSound()
            }

            if (gamePaused) { // Questo è il trigger principale per i dialoghi
                if (gameWon) {
                    GameWonDialog(
                        onDismissRequest = onConfirmAndNavigateToMenu, // Chiude e naviga
                        score = score 
                    )
                } else if (gameResetRequest) {
                    ResetDialog(
                        onDismissRequest = onCancelResetDialog,       // Annulla il reset e chiude il dialogo
                        onConfirmation = onConfirmAndNavigateToMenu // Conferma reset e naviga al menu
                    )
                } else {
                    PauseDialog(
                        onDismissRequest = onDismissPauseDialog     // Chiude il dialogo di pausa
                    )
                }
            }
            
            if (isBoardInitialized) {
                currentTablePlay?.let { board -> 
                    ShowTablePlay(
                        xDim = board.boardWidth,
                        yDim = board.boardHeight,
                        tablePlay = board, 
                        gameCardImages = gameCardImages,
                        checkPlayCardTurned = checkPlayCardTurned,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                } ?: run {
                    Log.e("GameApp", "CRITICAL: isBoardInitialized is true, but gameViewModel.tablePlay is null.")
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.game_error_board_null),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.game_loading_board),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Tail(
                actionOnPause = onPauseClicked, // Usa la nuova azione
                actionOnReset = onResetClicked  // Usa la nuova azione
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
    if (gameCardImages.isEmpty() && (xDim * yDim > 0)) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Card images not loaded for ShowTablePlay.", color = MaterialTheme.colorScheme.error)
        }
        return
    }
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
                Spacer(modifier = Modifier.height(16.dp))
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
                if (tablePlay.boardWidth > 0 && tablePlay.boardHeight > 1 && tablePlay.cardsArray.isNotEmpty() && tablePlay.cardsArray[0].size > 1) {
                    val cardState = tablePlay.cardsArray[0][1]
                    val cardValue = cardState.value
                    if (cardValue != null) {
                        Image(
                            painter = if (cardValue.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.clickable {
                                setPlayCardTurned(0, 1, !(cardValue.turned))
                            }
                        )
                    } else {
                        Text("Preview card value is null")
                    }
                } else {
                    Text("Preview card not available")
                }

            }
            if (tablePlay.boardWidth > 0 && tablePlay.boardHeight > 1 && tablePlay.cardsArray.isNotEmpty() && tablePlay.cardsArray[0].size > 1) {
                val cardValue = tablePlay.cardsArray[0][1].value
                Text(text = cardValue?.turned.toString() ?: "N/A")
            } else {
                 Text("Preview card data N/A")
            }
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                 if (tablePlay.boardWidth > 0 && tablePlay.boardHeight > 0 && tablePlay.cardsArray.isNotEmpty() && tablePlay.cardsArray[0].isNotEmpty()) {
                    val cardState = tablePlay.cardsArray[0][0]
                    val cardValue = cardState.value
                    if (cardValue != null) {
                        Image(
                            painter = if (cardValue.turned) painterResource(id = R.drawable.card_back) else painterResource(id = R.drawable.loading_image),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.clickable {
                                setPlayCardTurned(0, 0, !(cardValue.turned))
                            }
                        )
                    } else {
                        Text("Preview card value is null")
                    }
                } else {
                    Text("Preview card not available")
                }
            }
            if (tablePlay.boardWidth > 0 && tablePlay.boardHeight > 0 && tablePlay.cardsArray.isNotEmpty() && tablePlay.cardsArray[0].isNotEmpty()) {
                val cardValue = tablePlay.cardsArray[0][0].value
                 Text(text = cardValue?.turned.toString() ?: "N/A")
            } else {
                Text("Preview card data N/A")
            }
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
            Text("TestingDelayUsingList Preview (check implementation for details)")
        }
    }
}

@Preview
@Composable
fun TestingPreview() {
    MaterialTheme { 
        Column {
            Head(
                score = 0, 
                moves = 0,
                timeGame = "05:39"
            )

            val previewBoardWidth = 4
            val previewBoardHeight = 5
            val tablePlay = GameBoard(boardWidth = previewBoardWidth, boardHeight = previewBoardHeight)
            val allGameCardImages = GameCardImages().image 

            val uniqueCardsNeeded = (previewBoardWidth * previewBoardHeight) / 2
            val previewCardImages = if (allGameCardImages.size >= uniqueCardsNeeded) {
                allGameCardImages.distinct().take(uniqueCardsNeeded)
            } else {
                allGameCardImages.distinct().let { distinctImages ->
                    List(uniqueCardsNeeded) { distinctImages.getOrElse(it % distinctImages.size) { R.drawable.card_back } }
                }
            }

            val logicalCardIds = (0 until uniqueCardsNeeded).toList()
            val gameCardLogicalIdsForBoard = (logicalCardIds + logicalCardIds).shuffled()
            var cardIdx = 0
            for (x in 0 until previewBoardWidth) {
                for (y in 0 until previewBoardHeight) {
                    if (cardIdx < gameCardLogicalIdsForBoard.size) { 
                        val currentLogicalId = gameCardLogicalIdsForBoard.getOrElse(cardIdx++) { 0 } 
                        tablePlay.cardsArray[x][y].value = GameCard(
                            id = currentLogicalId,
                            turned = false, 
                            coupled = false
                        )
                    } else {
                        tablePlay.cardsArray[x][y].value = GameCard(id = 0, turned = true, coupled = true) 
                    }
                }
            }

            if (previewCardImages.size < uniqueCardsNeeded) {
                 Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Preview Error: Not enough unique images for the board size. Using placeholders.")
                 }
            } else {
                ShowTablePlay(
                    xDim = previewBoardWidth,
                    yDim = previewBoardHeight, 
                    tablePlay = tablePlay,
                    gameCardImages = previewCardImages, 
                    checkPlayCardTurned = {_, _ -> }, 
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
            Tail(
                actionOnPause = {},
                actionOnReset = {}
            )
        }
    }
}

// Assicurati che R.string.game_loading_board e R.string.game_error_board_null siano definite in strings.xml
