package com.example.moxmemorygame.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class ScoreEntry(
    val playerName: String,
    val score: Int,
    val timestamp: Long // Milliseconds since epoch
) {
    @Transient
    val dateTime: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    companion object {
        const val MAX_RANKING_ENTRIES = 10
    }
}