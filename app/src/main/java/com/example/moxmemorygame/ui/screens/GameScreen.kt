package com.example.moxmemorygame.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R
import com.example.moxmemorygame.model.GameFooterAction
import com.example.moxmemorygame.model.SoundEvent
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.IGameViewModel
import com.example.moxmemorygame.ui.SoundUtils
import com.example.moxmemorygame.ui.composables.BackgroundImg
import com.example.moxmemorygame.ui.composables.GameWonDialog
import com.example.moxmemorygame.ui.composables.Head
import com.example.moxmemorygame.ui.composables.PauseDialog
import com.example.moxmemorygame.ui.composables.ResetDialog
import com.example.moxmemorygame.ui.composables.ShowTablePlay
import com.example.moxmemorygame.ui.composables.Tail
import com.example.moxmemorygame.ui.formatDuration
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun GameScreen(
    innerPadding: PaddingValues, 
    modifier: Modifier = Modifier, 
    gameViewModel: IGameViewModel = koinViewModel<GameViewModel>(),
    soundUtils: SoundUtils = koinInject()
) {
    val onSoundEvent: (SoundEvent) -> Unit = { event ->
        soundUtils.playSound(event.resId)
    }

    val currentTablePlay = gameViewModel.tablePlay 
    val isBoardInitialized by gameViewModel.isBoardInitialized
    val playResetSound by gameViewModel.playResetSound.collectAsState()

    LaunchedEffect(playResetSound) {
        if (playResetSound) {
            onSoundEvent(SoundEvent.Reset)
            gameViewModel.onResetSoundPlayed()
        }
    }

    val checkPlayCardTurned = { x: Int, y: Int ->
        gameViewModel.checkGamePlayCardTurned(x, y, onSoundEvent)
    }

    // Actions for the dialogs
    val onDismissPauseDialog = { gameViewModel.dismissPauseDialog(); onSoundEvent(SoundEvent.Pause) }
    val onCancelResetDialog = { gameViewModel.cancelResetDialog(); onSoundEvent(SoundEvent.Pause) }
    val onConfirmAndNavigateToMenu = { gameViewModel.navigateToOpeningMenuAndCleanupDialogStates() }
    
    // A single, clean handler for all footer actions
    val onFooterAction = { action: GameFooterAction ->
        when (action) {
            GameFooterAction.Pause -> {
                gameViewModel.requestPauseDialog()
                onSoundEvent(SoundEvent.Pause)
            }
            GameFooterAction.Reset -> {
                gameViewModel.requestResetDialog()
                onSoundEvent(SoundEvent.Pause) // The same sound is used for pause and reset request
            }
        }
    }

    val gameCardImages = gameViewModel.gameCardImages 
    val gamePaused by gameViewModel.gamePaused 
    val gameResetRequest by gameViewModel.gameResetRequest 
    val gameWon by gameViewModel.gameWon 

    val score = gameViewModel.score.value
    val moves = gameViewModel.moves.value
    val timeGame by gameViewModel.currentTime.collectAsState()
    val timeGameString = timeGame.formatDuration()

    Box(
        modifier = Modifier 
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg(
            selectedBackgrounds = gameViewModel.selectedBackgrounds, 
            modifier = Modifier.fillMaxSize() 
        )
        Column(modifier = modifier) { 
            Head(
                score = score, 
                moves = moves,
                timeGame = timeGameString
            )

            if (gamePaused) { 
                if (gameWon) {
                    GameWonDialog(
                        onDismissRequest = onConfirmAndNavigateToMenu, 
                        score = score 
                    )
                } else if (gameResetRequest) {
                    ResetDialog(
                        onDismissRequest = onCancelResetDialog,       
                        onConfirmation = onConfirmAndNavigateToMenu
                    )
                } else {
                    PauseDialog(
                        onDismissRequest = onDismissPauseDialog     
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
                    Log.e("GameScreen", "CRITICAL: isBoardInitialized is true, but gameViewModel.tablePlay is null.")
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

            Tail(onAction = onFooterAction)
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }
}
