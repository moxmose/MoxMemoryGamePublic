package com.example.moxmemorygame.ui

import android.content.Context
import android.media.MediaPlayer
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.BackgroundMusic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class BackgroundMusicManager(
    private val context: Context,
    private val appSettingsDataStore: IAppSettingsDataStore,
    private val externalScope: CoroutineScope
) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackResId: Int? = null

    init {
        observeMusicPreferences()
    }

    private fun observeMusicPreferences() {
        externalScope.launch {
            combine(
                appSettingsDataStore.isMusicEnabled,
                appSettingsDataStore.selectedMusicTrackNames,
                appSettingsDataStore.musicVolume
            ) { isEnabled, trackNames, volume ->
                Triple(isEnabled, trackNames, volume)
            }.distinctUntilChanged().collect { (isEnabled, trackNames, volume) ->
                updateMusic(isEnabled, trackNames, volume)
            }
        }
    }

    private fun updateMusic(isEnabled: Boolean, trackNames: Set<String>, volume: Float) {
        if (!isEnabled || trackNames.isEmpty()) {
            stopAndRelease()
            return
        }

        val selectedTrackName = trackNames.random()
        val musicTrack = BackgroundMusic.fromTrackName(selectedTrackName)

        if (mediaPlayer == null || musicTrack.resId != currentTrackResId) {
            stopAndRelease()
            mediaPlayer = MediaPlayer.create(context, musicTrack.resId).apply {
                isLooping = true
                setVolume(volume, volume)
                start()
            }
            currentTrackResId = musicTrack.resId
        } else {
            mediaPlayer?.setVolume(volume, volume)
        }
    }

    fun onResume() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun onPause() {
        mediaPlayer?.pause()
    }

    private fun stopAndRelease() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackResId = null
    }
}
