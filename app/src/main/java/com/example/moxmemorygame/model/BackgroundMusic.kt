package com.example.moxmemorygame.model

import androidx.annotation.RawRes
import com.example.moxmemorygame.R

/**
 * Represents a background music track available in the app.
 *
 * @param trackName The unique string identifier for the track, used for saving preferences.
 * @param displayName The user-facing name for the track.
 * @param resId The raw resource ID of the music file. A value of -1 indicates no music.
 */
sealed class BackgroundMusic(val trackName: String, val displayName: String, @RawRes val resId: Int) {
    object None : BackgroundMusic("none", "None", -1)
    object ClassicSlowGuitar : BackgroundMusic("classic_slow_guitar", "Classic Slow Guitar", R.raw.backgroundmusic_classicslowguitar)
    object EasyRock : BackgroundMusic("easy_rock", "Easy Rock", R.raw.backgroundmusic_easyrock)
    object GuitarAndDrum : BackgroundMusic("guitar_and_drum", "Guitar and Drum", R.raw.backgroundmusic_guitaranddrum)
    object GuitarRiffle : BackgroundMusic("guitar_riffle", "Guitar riffle", R.raw.backgroundmusic_guitarriffle)
    object HappyRhythm : BackgroundMusic("happy_rhythm", "Happy rhythm", R.raw.backgroundmusic_happyrhythm)
    object SentimentalClassicLowGuitar : BackgroundMusic("sentimental_classic_low_guitar", "Sentimental Classic low Guitar", R.raw.backgroundmusic_sentimentalclassicslowguitar)
    object SentimentalRock : BackgroundMusic("sentimental_rock", "Sentimental Rock", R.raw.backgroundmusic_sentimentalrock)
    object SlowSynth : BackgroundMusic("slow_synth", "Slow Synth", R.raw.backgroundmusic_slowsynth)

    companion object {
        /**
         * A list of all available background music tracks, including the option to have none.
         */
        val allTracks = listOf(
            None,
            ClassicSlowGuitar,
            EasyRock,
            GuitarAndDrum,
            GuitarRiffle,
            HappyRhythm,
            SentimentalClassicLowGuitar,
            SentimentalRock,
            SlowSynth
        )

        /**
         * A set of all music track names, used as the default selection.
         */
        val allTrackNames: Set<String> = allTracks.filter { it != None }.map { it.trackName }.toSet()

        /**
         * Finds a BackgroundMusic instance by its track name identifier.
         * @param trackName The string identifier to find.
         * @return The corresponding [BackgroundMusic] object, or [None] if not found.
         */
        fun fromTrackName(trackName: String?): BackgroundMusic {
            return allTracks.find { it.trackName == trackName } ?: None
        }
        
        /**
         * Finds a BackgroundMusic instance by its resource ID.
         * @param resId The raw resource ID to find.
         * @return The corresponding [BackgroundMusic] object, or [None] if not found.
         */
        fun fromResId(resId: Int): BackgroundMusic {
            return allTracks.find { it.resId == resId } ?: None
        }
    }
}
