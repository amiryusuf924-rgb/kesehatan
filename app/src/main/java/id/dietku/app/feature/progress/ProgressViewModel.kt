package id.dietku.app.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.DailyTrackingRepository
import id.dietku.app.data.FastingRepository
import id.dietku.app.data.MealLogRepository
import id.dietku.app.data.ProfileRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val dailyTrackingRepository: DailyTrackingRepository,
    private val fastingRepository: FastingRepository,
    mealLogRepository: MealLogRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    val weights = dailyTrackingRepository.observeWeights().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val profile = profileRepository.observeProfile().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )
    val weeklyMeals = mealLogRepository.observeRange(
        LocalDate.now().minusDays(6),
        LocalDate.now().plusDays(1),
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val fastingSessions = fastingRepository.observeRecent().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun addWeight(weightKg: Double, measuredAtEpochMillis: Long) {
        if (weightKg <= 0.0) return
        viewModelScope.launch {
            dailyTrackingRepository.logWeight(weightKg, measuredAtEpochMillis)
            val latestTimestamp = weights.value.maxOfOrNull { it.measuredAtEpochMillis }
            if (latestTimestamp == null || measuredAtEpochMillis >= latestTimestamp) {
                profileRepository.updateCurrentWeight(weightKg)
            }
        }
    }
}