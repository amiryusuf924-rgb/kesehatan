package id.dietku.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "dietku_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val fastingHoursKey = intPreferencesKey("fasting_hours")
    private val eatingStartHourKey = intPreferencesKey("eating_start_hour")
    private val relaxedDateKey = stringPreferencesKey("relaxed_date")
    private val stepBaselineDateKey = stringPreferencesKey("step_baseline_date")
    private val stepBaselineCountKey = floatPreferencesKey("step_baseline_count")
    private val quietStartHourKey = intPreferencesKey("quiet_start_hour")
    private val quietEndHourKey = intPreferencesKey("quiet_end_hour")

    val darkMode: Flow<Boolean?> = context.settingsDataStore.data.map { preferences ->
        preferences[darkModeKey]
    }

    val fastingPreferences: Flow<FastingPreferences> = context.settingsDataStore.data.map { preferences ->
        FastingPreferences(
            fastingHours = preferences[fastingHoursKey] ?: 16,
            eatingStartHour = preferences[eatingStartHourKey] ?: 11,
            relaxedDate = preferences[relaxedDateKey],
        )
    }

    val stepBaseline: Flow<StepBaseline> = context.settingsDataStore.data.map { preferences ->
        StepBaseline(
            date = preferences[stepBaselineDateKey],
            count = preferences[stepBaselineCountKey],
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }

    suspend fun setFastingWindow(fastingHours: Int, eatingStartHour: Int) {
        require(fastingHours in setOf(12, 14, 16, 18))
        require(eatingStartHour in 0..23)
        context.settingsDataStore.edit { preferences ->
            preferences[fastingHoursKey] = fastingHours
            preferences[eatingStartHourKey] = eatingStartHour
        }
    }

    suspend fun relaxFastingToday() {
        context.settingsDataStore.edit { preferences ->
            preferences[relaxedDateKey] = LocalDate.now().toString()
        }
    }

    suspend fun saveStepBaseline(date: String, count: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[stepBaselineDateKey] = date
            preferences[stepBaselineCountKey] = count
        }
    }

    suspend fun setQuietHours(startHour: Int, endHour: Int) {
        require(startHour in 0..23 && endHour in 0..23)
        context.settingsDataStore.edit { preferences ->
            preferences[quietStartHourKey] = startHour
            preferences[quietEndHourKey] = endHour
        }
    }

    suspend fun isQuietNow(time: LocalTime = LocalTime.now()): Boolean {
        val preferences = context.settingsDataStore.data.first()
        val start = preferences[quietStartHourKey] ?: 8
        val end = preferences[quietEndHourKey] ?: 16
        return if (start == end) {
            false
        } else if (start < end) {
            time.hour in start until end
        } else {
            time.hour >= start || time.hour < end
        }
    }

    val quietHours: Flow<Pair<Int, Int>> = context.settingsDataStore.data.map { preferences ->
        (preferences[quietStartHourKey] ?: 8) to (preferences[quietEndHourKey] ?: 16)
    }
}

data class FastingPreferences(
    val fastingHours: Int = 16,
    val eatingStartHour: Int = 11,
    val relaxedDate: String? = null,
) {
    fun fastingHoursFor(date: LocalDate = LocalDate.now()): Int =
        if (relaxedDate == date.toString()) 14 else fastingHours

    fun eatingHoursFor(date: LocalDate = LocalDate.now()): Int = 24 - fastingHoursFor(date)
}

data class StepBaseline(val date: String? = null, val count: Float? = null)