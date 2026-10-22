package top.ekiz.whitenoise.domain

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.ekiz.whitenoise.data.SettingsDataStore

enum class TimerEvent {
    START_FADE_OUT,
    TIMER_FINISHED,
    TIMER_CANCELLED
}

@Singleton
class TimerManager @Inject constructor(private val settingsDataStore: SettingsDataStore) {
    private val _activeRemainingMillis = MutableStateFlow(0L)
    val activeRemainingMillis = _activeRemainingMillis.asStateFlow()

    private val _totalTimerMillis = MutableStateFlow(0L)
    val totalTimerMillis = _totalTimerMillis.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _timerEvents =
        MutableSharedFlow<TimerEvent>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val timerEvents = _timerEvents.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null

    init {

        scope.launch {
            val total = settingsDataStore.totalTimerMillisFlow.first()
            if (total > 0L) {
                _totalTimerMillis.value = total
            }

            val endTime = settingsDataStore.timerEndTimeFlow.first()
            if (endTime > 0L) {
                val remaining = endTime - System.currentTimeMillis()
                if (remaining > 0) {

                    startTicker(remaining, endTime)
                } else {

                    clearTimer()
                }
            } else {

                val remaining = settingsDataStore.timerRemainingFlow.first()
                if (remaining > 0L) {
                    _activeRemainingMillis.value = remaining
                } else {
                    _totalTimerMillis.value = 0L
                }
            }
        }
    }

    fun startTimer(durationMs: Long, isResume: Boolean = false) {
        if (durationMs <= 0) return

        scope.launch {
            var newTotal: Long? = null
            if (!isResume || _totalTimerMillis.value <= 0L) {
                _totalTimerMillis.value = durationMs
                newTotal = durationMs
            }
            val endTime = System.currentTimeMillis() + durationMs

            settingsDataStore.saveTimerState(endTime = endTime, remaining = 0L, totalMillis = newTotal)

            startTicker(durationMs, endTime)
        }
    }

    fun pauseTimer() {
        tickerJob?.cancel()

        scope.launch {
            val remaining = _activeRemainingMillis.value
            _isTimerRunning.value = false

            settingsDataStore.saveTimerState(endTime = 0L, remaining = remaining.coerceAtLeast(0L))

            _timerEvents.tryEmit(TimerEvent.TIMER_CANCELLED)
        }
    }

    /** Replaces the time left on a paused timer, so resuming it runs for the new duration. */
    fun setPausedDuration(durationMs: Long) {
        if (durationMs <= 0) {
            cancelTimer()
            return
        }
        _totalTimerMillis.value = durationMs
        _activeRemainingMillis.value = durationMs

        scope.launch {
            settingsDataStore.saveTimerState(
                endTime = 0L,
                remaining = durationMs,
                totalMillis = durationMs
            )
        }
    }

    fun cancelTimer() {
        tickerJob?.cancel()

        scope.launch {
            clearTimer()
            _timerEvents.tryEmit(TimerEvent.TIMER_CANCELLED)
        }
    }

    private suspend fun clearTimer() {
        _activeRemainingMillis.value = 0L
        _totalTimerMillis.value = 0L
        _isTimerRunning.value = false
        settingsDataStore.saveTimerState(endTime = 0L, remaining = 0L, totalMillis = 0L)
    }

    private fun startTicker(durationMs: Long, endTime: Long) {
        tickerJob?.cancel()
        _isTimerRunning.value = true
        _activeRemainingMillis.value = durationMs

        tickerJob =
            scope.launch {
                var fadeTriggered = false

                while (true) {
                    val remaining = endTime - System.currentTimeMillis()

                    if (remaining <= 60000L && !fadeTriggered) {
                        fadeTriggered = true
                        _timerEvents.tryEmit(TimerEvent.START_FADE_OUT)
                    }

                    if (remaining > 0) {
                        _activeRemainingMillis.value = remaining
                    } else {
                        clearTimer()
                        _timerEvents.tryEmit(TimerEvent.TIMER_FINISHED)
                        break
                    }
                    delay(1000)
                }
            }
    }
}
