package com.example.moxmemorygame.ui

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.BackgroundMusic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BackgroundMusicManager(
    private val context: Context,
    private val appSettingsDataStore: IAppSettingsDataStore,
    private val externalScope: CoroutineScope
) {

    private var mainPlayer: MediaPlayer? = null
    private var previewPlayer: MediaPlayer? = null

    private var currentTrackResId: Int? = null
    private var isMainPlayerPausedByPreview = false

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
                updateMusicState(isEnabled, trackNames, volume)
            }
        }
    }

    private fun updateMusicState(isEnabled: Boolean, trackNames: Set<String>, volume: Float) {
        // Case 1: Music should be off
        if (!isEnabled || trackNames.isEmpty()) {
            stopAndReleaseMainPlayer()
            return
        }

        // If a player is active, check if its track is still in the selection
        if (mainPlayer != null && currentTrackResId != null) {
            val currentTrack = BackgroundMusic.fromResId(currentTrackResId!!)
            if (currentTrack.trackName !in trackNames) {
                // The currently playing track was deselected by the user. Play a new one.
                playNewRandomTrack(trackNames, volume)
            } else {
                // The track is still valid, just update the volume.
                mainPlayer?.setVolume(volume, volume)
            }
        } else {
            // No music is playing, so start a new track.
            playNewRandomTrack(trackNames, volume)
        }
    }

    private fun playNewRandomTrack(trackNames: Set<String>, volume: Float) {
        if (trackNames.isEmpty()) {
            stopAndReleaseMainPlayer()
            return
        }
        
        val selectedTrackName = trackNames.random()
        val musicTrack = BackgroundMusic.fromTrackName(selectedTrackName)
        
        if(musicTrack.resId == -1) {
            stopAndReleaseMainPlayer()
            return
        }
        
        stopAndReleaseMainPlayer() // Ensure any old instance is gone

        val player = try {
            MediaPlayer.create(context, musicTrack.resId)
        } catch (e: Exception) {
            Log.e("MusicManager", "Failed to create MediaPlayer for resId ${musicTrack.resId}", e)
            null
        }

        if (player == null) {
            Log.e("MusicManager", "MediaPlayer.create returned null. Check resource validity.")
            currentTrackResId = null
            return
        }

        mainPlayer = player.apply {
            isLooping = false // We handle shuffle manually
            setVolume(volume, volume)
            setOnCompletionListener { onTrackCompleted() }
            start()
        }
        currentTrackResId = musicTrack.resId
    }

    private fun onTrackCompleted() {
        externalScope.launch {
            val isEnabled = appSettingsDataStore.isMusicEnabled.first()
            val trackNames = appSettingsDataStore.selectedMusicTrackNames.first()
            val volume = appSettingsDataStore.musicVolume.first()
            if (isEnabled && trackNames.isNotEmpty()) {
                playNewRandomTrack(trackNames, volume)
            }
        }
    }

    fun playPreview(track: BackgroundMusic) {
        externalScope.launch {
            if (mainPlayer?.isPlaying == true) {
                mainPlayer?.pause()
                isMainPlayerPausedByPreview = true
            }

            stopAndReleasePreviewPlayer()
            
            val player = try {
                MediaPlayer.create(context, track.resId)
            } catch (e: Exception) {
                Log.e("MusicManager", "Failed to create preview MediaPlayer for resId ${track.resId}", e)
                null
            }

            if (player == null) {
                Log.e("MusicManager", "Preview MediaPlayer.create returned null.")
                return@launch
            }

            previewPlayer = player.apply {
                isLooping = false
                val volume = appSettingsDataStore.musicVolume.first()
                setVolume(volume, volume)
                start()
            }
        }
    }

    fun stopPreview() {
        stopAndReleasePreviewPlayer()
        if (isMainPlayerPausedByPreview) {
            mainPlayer?.start()
            isMainPlayerPausedByPreview = false
        }
    }

    fun onResume() {
        if (isMainPlayerPausedByPreview) return // Don't resume main music if a preview is active
        if (mainPlayer?.isPlaying == false) {
            mainPlayer?.start()
        }
    }

    fun onPause() {
        mainPlayer?.pause()
        // Previews are short-lived, so we can just stop them completely
        stopAndReleasePreviewPlayer()
        isMainPlayerPausedByPreview = false
    }

    private fun stopAndReleaseMainPlayer() {
        mainPlayer?.release()
        mainPlayer = null
        currentTrackResId = null
    }

    private fun stopAndReleasePreviewPlayer() {
        previewPlayer?.release()
        previewPlayer = null
    }
}