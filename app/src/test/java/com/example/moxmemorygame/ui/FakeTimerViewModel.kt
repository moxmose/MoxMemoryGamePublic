package com.example.moxmemorygame.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of TimerViewModel for testing purposes.
 * It inherits from the real TimerViewModel to be a valid substitute.
 */
class FakeTimerViewModel : TimerViewModel() {
    private val _elapsedSeconds = MutableStateFlow(0L)
    override val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var isRunning = false

    override fun startTimer() {
        isRunning = true
    }

    override fun stopTimer() {
        isRunning = false
    }

    override fun resetTimer() {
        _elapsedSeconds.value = 0L
        isRunning = false
    }

    override fun isTimerRunning(): Boolean = isRunning

    /**
     * Sets a specific elapsed time for testing.
     */
    fun setElapsedTime(seconds: Long) {
        _elapsedSeconds.value = seconds
    }

    override suspend fun stopAndAwaitTimerCompletion() {
        stopTimer()
    }
}