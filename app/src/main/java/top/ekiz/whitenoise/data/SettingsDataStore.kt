package top.ekiz.whitenoise.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import top.ekiz.whitenoise.domain.NoiseType

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val VOLUME_KEY = floatPreferencesKey("volume")
        val NOISE_TYPE_KEY = stringPreferencesKey("noise_type")
        val SLEEP_TIMER_KEY = intPreferencesKey("sleep_timer")
        val TIMER_END_TIME_KEY = longPreferencesKey("timer_end_time")
        val TIMER_REMAINING_KEY = longPreferencesKey("timer_remaining")
        val TOTAL_TIMER_MILLIS_KEY = longPreferencesKey("total_timer_millis")
        val BALANCE_KEY = floatPreferencesKey("balance")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val SPATIAL_AUDIO_KEY = booleanPreferencesKey("spatial_audio")
    }

    private fun <T> flowOf(key: Preferences.Key<T>, default: T): Flow<T> =
        context.dataStore.data.map { it[key] ?: default }.distinctUntilChanged()

    private suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    val volumeFlow: Flow<Float> = flowOf(VOLUME_KEY, 0.5f)

    val noiseTypeFlow: Flow<NoiseType> =
        flowOf(NOISE_TYPE_KEY, NoiseType.WHITE.name)
            .map { name -> NoiseType.entries.firstOrNull { it.name == name } ?: NoiseType.WHITE }

    val sleepTimerFlow: Flow<Int> = flowOf(SLEEP_TIMER_KEY, 0)
    val timerEndTimeFlow: Flow<Long> = flowOf(TIMER_END_TIME_KEY, 0L)
    val timerRemainingFlow: Flow<Long> = flowOf(TIMER_REMAINING_KEY, 0L)
    val totalTimerMillisFlow: Flow<Long> = flowOf(TOTAL_TIMER_MILLIS_KEY, 0L)
    val balanceFlow: Flow<Float> = flowOf(BALANCE_KEY, 0f)
    val themeModeFlow: Flow<String> = flowOf(THEME_MODE_KEY, "System")
    val spatialAudioFlow: Flow<Boolean> = flowOf(SPATIAL_AUDIO_KEY, false)

    suspend fun saveVolume(volume: Float) = save(VOLUME_KEY, volume)

    suspend fun saveNoiseType(noiseType: NoiseType) = save(NOISE_TYPE_KEY, noiseType.name)

    suspend fun saveSleepTimer(minutes: Int) = save(SLEEP_TIMER_KEY, minutes)

    suspend fun saveBalance(balance: Float) = save(BALANCE_KEY, balance)

    suspend fun saveThemeMode(mode: String) = save(THEME_MODE_KEY, mode)

    suspend fun saveSpatialAudio(enabled: Boolean) = save(SPATIAL_AUDIO_KEY, enabled)

    suspend fun saveTimerState(endTime: Long, remaining: Long, totalMillis: Long? = null) {
        context.dataStore.edit {
            it[TIMER_END_TIME_KEY] = endTime
            it[TIMER_REMAINING_KEY] = remaining
            if (totalMillis != null) it[TOTAL_TIMER_MILLIS_KEY] = totalMillis
        }
    }
}
