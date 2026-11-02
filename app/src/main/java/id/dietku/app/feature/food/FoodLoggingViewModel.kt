package id.dietku.app.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.MealLogRepository
import id.dietku.app.data.SettingsRepository
import id.dietku.app.data.local.FoodItemEntity
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FoodLoggingViewModel @Inject constructor(
    private val repository: MealLogRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val foods = repository.observeFoods().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val todayLogs = repository.observeDay(LocalDate.now()).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val fastingPreferences = settingsRepository.fastingPreferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        id.dietku.app.data.FastingPreferences(),
    )

    fun logFood(food: FoodItemEntity, quantity: Double, mealType: String) {
        if (quantity <= 0.0) return
        viewModelScope.launch { repository.logFood(food, quantity, mealType) }
    }

    fun addCustomFood(food: FoodItemEntity) {
        viewModelScope.launch { repository.addCustomFood(food) }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch { repository.deleteLog(id) }
    }

    fun copyYesterday() {
        viewModelScope.launch { repository.copyYesterday() }
    }
}