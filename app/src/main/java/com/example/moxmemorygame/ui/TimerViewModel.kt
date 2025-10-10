package com.example.moxmemorygame.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * [TimerViewModel] is a ViewModel responsible for managing a timer.
 * It provides functionalities to start, stop, reset, and check the status of the timer.
 * It exposes the elapsed time as a StateFlow for UI consumption.
 */
open class TimerViewModel: ViewModel() {
    private val _elapsedSeconds = MutableStateFlow(0L)
    open val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var isPaused: Boolean = false

    /**
     * Starts a timer that increments the elapsed seconds every second.
     *
     * This function does the following:
     * 1. **Cancels any existing timer:** If a timer is already running (i.e., `timerJob` is not null),
     *    it is canceled to prevent multiple timers from running concurrently.
     * 2. **Launches a new coroutine:** A new coroutine is launched within the `viewModelScope` to
     *    manage the timer's execution. This ensures that the timer is tied to the lifecycle of the
     *    ViewModel.
     * 3. **Enters an infinite loop:** The coroutine enters a `while (isActive)` loop, which continues
     *    to run as long as the coroutine is active.
     * 4. **Delays for one second:** Inside the loop, `delay(1000L)` pauses the coroutine for one
     *    second (1000 milliseconds).
     * 5. **Increments elapsed seconds:** After the delay, `_elapsedSeconds.value++` increments the
     *    value of a mutable state (presumably a LiveData or StateFlow) representing the number of
     *    seconds elapsed.
     *
     * The timer will continue to run until one of the following occurs:
     * - The `viewModelScope` is cancelled (e.g., when the ViewModel is cleared).
     * - The `timerJob` is explicitly cancelled using `timerJob?.cancel()`.
     * - The coroutine's `isActive` flag becomes false for another reason.
     *
     * **Note:** This function assumes the existence of:
     *   - `timerJob`: A `Job` used to manage the timer coroutine.
     *   - `viewModelScope`: A `CoroutineScope` tied to the ViewModel's lifecycle.
     *   - `_elapsedSeconds`: A mutable state holder (e.g., MutableLiveData, MutableStateFlow)
     *     that stores the number of elapsed seconds.
     */
    open fun startTimer() {
        timerJob?.cancel()
        isPaused = false
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if(!isPaused){
                    _elapsedSeconds.value++
                }
            }


        }
    }

    /**
     * Stops the currently running timer, if any.
     *
     * This function cancels the `timerJob` coroutine, effectively stopping the timer.
     * It also sets `timerJob` to `null` to indicate that no timer is currently active.
     *
     * If no timer is running (i.e., `timerJob` is already `null`), calling this function has no effect.
     */
    open fun stopTimer() {
        timerJob?.cancel()
        isPaused = true
        timerJob = null
    }

    /**
     * Resets the timer.
     *
     * Sets the elapsed time to 0 and stops the timer.
     */
    open fun resetTimer() {
        _elapsedSeconds.value = 0L
        stopTimer()
    }

    /**
     * Stops the currently running timer and waits for its completion.
     *
     * This function gracefully stops the timer that is associated with [timerJob].
     * If a timer is currently running, it will be cancelled. The function then suspends
     * until the timer's coroutine has finished execution (if it was running).
     * After completion, the [timerJob] is set to null, indicating that no timer is currently active.
     *
     * This method should be called to ensure that any ongoing timer tasks are properly
     * terminated and resources are released.
     *
     * Example Usage:
     * ```
     *  // Assuming you have a timerJob of type Job initialized elsewhere
     *  timerJob = someCoroutineScope.launch {
     *      // timer logic here
     *      while(isActive){
     *         // do stuff
     *         delay(1000L)
     *      }
     *  }
     *
     *  // Later, when you want to stop the timer:
     *  stopAndAwaitTimerCompletion()
     * ```
     *
     * Note: This function is safe to call even if [timerJob] is null (i.e., no timer is running).
     * In this case, the function will simply do nothing.
     *
     * @see Job.cancelAndJoin
     */
    open suspend fun stopAndAwaitTimerCompletion() {
        timerJob?.cancelAndJoin()
        isPaused = true
        timerJob = null
    }

    /**
     * Checks if the timer is currently running.
     *
     * @return `true` if the timer is running, `false` otherwise.
     */
    open fun isTimerRunning(): Boolean {
        return timerJob?.isActive ?: false
    }

    /**
     * Gets the elapsed time in seconds.
     *
     * @return The elapsed time in seconds.
     */
    open fun getElapsedTime(): Long {
        return _elapsedSeconds.value
    }

    open fun pauseTimer(){
        isPaused = true
    }

    open fun resumeTimer(){
        isPaused = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            stopAndAwaitTimerCompletion()
        }
    }
}

/**
 * Utility to format a duration in seconds to a time string (HH:MM:SS or MM:SS).
 */
fun Long.formatDuration(showHours: Boolean = false): String {
    require(this >= 0) { "Duration must be non-negative" }

    // API<26
    val totalSeconds = this
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (showHours) {
        String.format(java.util.Locale.UK, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        if (hours > 0) {
            // If hours are present but not displayed, show a placeholder
            "99:99"
        } else {
            String.format(java.util.Locale.UK, "%02d:%02d", minutes, seconds)
        }
    }
}