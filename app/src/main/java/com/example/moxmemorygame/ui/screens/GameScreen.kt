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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moxmemorygame.R
import com.example.moxmemorygame.ui.GameViewModel
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

@Composable
fun GameScreen(
    innerPadding: PaddingValues, 
    modifier: Modifier = Modifier, 
    gameViewModel: GameViewModel = koinViewModel()
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
    val onResetClicked = { gameViewModel.requestResetDialog(); pauseSound() } 

    // Azioni per i dialoghi
    val onDismissPauseDialog = { gameViewModel.dismissPauseDialog(); pauseSound() }
    val onCancelResetDialog = { gameViewModel.cancelResetDialog(); pauseSound() }
    val onConfirmAndNavigateToMenu = { gameViewModel.navigateToOpeningMenuAndCleanupDialogStates(); }

    val gameCardImages = gameViewModel.gameCardImages 
    val gamePaused by gameViewModel.gamePaused 
    val gameResetRequest by gameViewModel.gameResetRequest 
    val gameWon by gameViewModel.gameWon 
    val gamePlayResetSound = gameViewModel.gamePlayResetSound 
    val resetPlayResetSound = { gameViewModel.resetPlayResetSound(resetSound) }

    val score = gameViewModel.score.intValue
    val moves = gameViewModel.moves.intValue
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

            if (gamePlayResetSound) {
                resetPlayResetSound()
            }

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

            Tail(
                actionOnPause = onPauseClicked, 
                actionOnReset = onResetClicked  
            )
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }
}
