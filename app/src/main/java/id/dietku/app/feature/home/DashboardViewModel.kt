package id.dietku.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.DailyTrackingRepository
import id.dietku.app.data.MealLogRepository
import id.dietku.app.data.ProfileRepository
import id.dietku.app.data.SettingsRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dailyTrackingRepository: DailyTrackingRepository,
    mealLogRepository: MealLogRepository,
    settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    val meals = mealLogRepository.observeDay(LocalDate.now()).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val water = dailyTrackingRepository.observeWater().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val exercise = dailyTrackingRepository.observeExercise().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val fastingPreferences = settingsRepository.fastingPreferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        id.dietku.app.data.FastingPreferences(),
    )

    fun addWater(amountMl: Int = 250) {
        viewModelScope.launch { dailyTrackingRepository.addWater(amountMl) }
    }

    fun completeTodayWorkout() {
        val (name, minutes) = when (LocalDate.now().dayOfWeek) {
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY -> "Jalan cepat" to 25
            DayOfWeek.TUESDAY -> "Jalan santai + peregangan" to 20
            DayOfWeek.THURSDAY -> "Jalan ringan" to 20
            DayOfWeek.FRIDAY -> "Jalan cepat interval" to 25
            DayOfWeek.SATURDAY -> "Jalan cepat/santai" to 20
            DayOfWeek.SUNDAY -> "Sepeda atau jalan jauh" to 35
        }
        viewModelScope.launch { dailyTrackingRepository.logExercise(name, minutes) }
    }

    fun logWeight(weightKg: Double) {
        viewModelScope.launch {
            dailyTrackingRepository.logWeight(weightKg)
            profileRepository.updateCurrentWeight(weightKg)
        }
    }
}