package id.dietku.app.feature.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.DailyTrackingRepository
import id.dietku.app.data.SettingsRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val dailyTrackingRepository: DailyTrackingRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val monday = LocalDate.now().with(DayOfWeek.MONDAY)
    val weekLogs = dailyTrackingRepository.observeExerciseRange(monday, monday.plusDays(7)).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val stepBaseline = settingsRepository.stepBaseline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        id.dietku.app.data.StepBaseline(),
    )

    fun completeWorkout(durationMinutes: Int) {
        setWorkoutCompleted(LocalDate.now(), true, durationMinutes)
    }

    fun setWorkoutCompleted(date: LocalDate, completed: Boolean, durationMinutes: Int) {
        viewModelScope.launch {
            val existing = weekLogs.value.firstOrNull { log ->
                java.time.Instant.ofEpochMilli(log.loggedAtEpochMillis)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate() == date && log.completed
            }
            if (completed && existing == null) {
                val workout = workoutFor(date.dayOfWeek)
                val timestamp = date.atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                dailyTrackingRepository.logExercise(workout.first, durationMinutes, timestamp)
            } else if (!completed && existing != null) {
                dailyTrackingRepository.deleteExercise(existing.id)
            }
        }
    }

    fun saveStepBaseline(today: String, count: Float) {
        viewModelScope.launch { settingsRepository.saveStepBaseline(today, count) }
    }

    companion object {
        fun workoutFor(day: DayOfWeek): Pair<String, Int> = when (day) {
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY -> "Jalan cepat" to 25
            DayOfWeek.TUESDAY -> "Jalan santai + peregangan" to 20
            DayOfWeek.THURSDAY -> "Jalan ringan" to 20
            DayOfWeek.FRIDAY -> "Jalan cepat interval" to 25
            DayOfWeek.SATURDAY -> "Jalan cepat/santai" to 20
            DayOfWeek.SUNDAY -> "Sepeda atau jalan jauh" to 35
        }
    }
}