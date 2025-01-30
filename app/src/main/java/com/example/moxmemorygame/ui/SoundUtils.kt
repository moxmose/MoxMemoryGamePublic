package com.example.moxmemorygame.ui

import android.content.Context
import android.media.MediaPlayer

object SoundUtils {
    var mediaPlayer: MediaPlayer? = null
    fun playSound(context: Context, soundResId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            releaseMediaPlayer()
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}