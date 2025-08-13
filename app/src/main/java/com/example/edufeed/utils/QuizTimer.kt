package com.example.edufeed.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.nanoseconds

/**
 * Manages the quiz timer with pause/resume functionality
 */
class QuizTimer(
    private val duration: Duration,
    private val onTimeUp: () -> Unit,
    private val coroutineScope: CoroutineScope,
    private val tickInterval: Duration = 1.seconds
) {
    private var startTime: Long = 0L
    private var elapsedTime: Duration = Duration.ZERO
    private var isRunning = false
    private var job: Job? = null

    private val _timeRemaining = MutableStateFlow(duration)
    val timeRemaining: StateFlow<Duration> = _timeRemaining.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    init {
        reset()
    }

    /**
     * Starts or resumes the timer
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        _isPaused.value = false
        startTime = System.nanoTime() - elapsedTime.inWholeNanoseconds
        
        job = coroutineScope.launch {
            while (isActive && isRunning) {
                val currentTime = System.nanoTime()
                elapsedTime = (currentTime - startTime).nanoseconds
                
                val remaining = (duration - elapsedTime).coerceAtLeast(Duration.ZERO)
                _timeRemaining.value = remaining
                
                if (remaining == Duration.ZERO) {
                    stop()
                    onTimeUp()
                    break
                }
                
                delay(tickInterval)
            }
        }
    }

    /**
     * Pauses the timer
     */
    fun pause() {
        if (!isRunning) return
        
        isRunning = false
        _isPaused.value = true
        job?.cancel()
    }

    /**
     * Stops the timer and resets it
     */
    fun stop() {
        isRunning = false
        _isPaused.value = false
        job?.cancel()
        reset()
    }

    /**
     * Resets the timer to its initial state
     */
    fun reset() {
        isRunning = false
        _isPaused.value = false
        elapsedTime = Duration.ZERO
        _timeRemaining.value = duration
        job?.cancel()
    }
    
    /**
     * Updates the duration and resets the timer
     */
    fun updateDuration(newDuration: Duration) {
        stop()
        _timeRemaining.value = newDuration
    }
    
    /**
     * Formats the remaining time as MM:SS
     */
    fun formatTime(remaining: Duration): String {
        val minutes = remaining.inWholeMinutes
        val seconds = (remaining.inWholeSeconds % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Cleans up resources
     */
    fun dispose() {
        stop()
    }
}
