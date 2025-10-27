package com.example.moxmemorygame.model

import androidx.annotation.RawRes
import com.example.moxmemorygame.R

/**
 * Represents all possible sound events that can occur during the game.
 * Each event holds a direct reference to its corresponding raw sound resource ID.
 * This keeps the sound mapping logic co-located with the event definition.
 */
sealed class SoundEvent(@RawRes val resId: Int) {
    object Flip : SoundEvent(R.raw.flipcard)
    object Success : SoundEvent(R.raw.short_success_sound_glockenspiel_treasure_videogame)
    object Fail : SoundEvent(R.raw.fail)
    object Win : SoundEvent(R.raw.brass_fanfare_with_timpani_and_winchimes_reverberated)
    object Pause : SoundEvent(R.raw.keyswipe_card)
    object Reset : SoundEvent(R.raw.card_mixing)

    companion object {
        // A list of all sound events, used to preload them into the SoundPool.
        val allEvents = listOf(Flip, Success, Fail, Win, Pause, Reset)
    }
}
