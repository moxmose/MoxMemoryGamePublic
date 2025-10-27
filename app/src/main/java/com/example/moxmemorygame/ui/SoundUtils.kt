package com.example.moxmemorygame.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.moxmemorygame.data.local.IAppSettingsDataStore
import com.example.moxmemorygame.model.SoundEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SoundUtils(
    context: Context,
    private val appSettingsDataStore: IAppSettingsDataStore,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val soundPool: SoundPool
    private val soundIdMap = mutableMapOf<Int, Int>()
    private var isLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Allow up to 5 simultaneous sound effects
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            } else {
                Log.e("SoundUtils", "Failed to load a sound, status: $status")
            }
        }

        loadSounds(context)
    }

    private fun loadSounds(context: Context) {
        SoundEvent.allEvents.forEach { event ->
            if (event.resId != -1) {
                val soundId = soundPool.load(context, event.resId, 1)
                soundIdMap[event.resId] = soundId
            }
        }
    }

    fun playSound(soundResId: Int) {
        externalScope.launch {
            val areEffectsEnabled = appSettingsDataStore.areSoundEffectsEnabled.first()
            if (!areEffectsEnabled || !isLoaded) return@launch

            val volume = appSettingsDataStore.soundEffectsVolume.first()
            val soundId = soundIdMap[soundResId]

            if (soundId != null) {
                soundPool.play(soundId, volume, volume, 1, 0, 1f)
            } else {
                Log.w("SoundUtils", "Sound with resId $soundResId not found in sound pool map.")
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}