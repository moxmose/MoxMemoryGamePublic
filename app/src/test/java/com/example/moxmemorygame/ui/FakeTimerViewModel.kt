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

    override fun startTimer() {
        // In the fake, this does nothing
    }

    override fun stopTimer() {
        // In the fake, this does nothing
    }

    override fun resetTimer() {
        _elapsedSeconds.value = 0L
    }

    override suspend fun stopAndAwaitTimerCompletion() {
        // In the fake, this does nothing
    }
}