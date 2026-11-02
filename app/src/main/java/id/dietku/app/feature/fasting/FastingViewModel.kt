package id.dietku.app.feature.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.FastingRepository
import id.dietku.app.data.SettingsRepository
import id.dietku.app.data.FastingPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FastingViewModel @Inject constructor(
    private val fastingRepository: FastingRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val preferences = settingsRepository.fastingPreferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FastingPreferences(),
    )
    val activeSession = fastingRepository.observeActive().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )
    val recentSessions = fastingRepository.observeRecent().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun setSchedule(fastingHours: Int, eatingStartHour: Int) {
        viewModelScope.launch { settingsRepository.setFastingWindow(fastingHours, eatingStartHour) }
    }

    fun relaxToday() {
        viewModelScope.launch { settingsRepository.relaxFastingToday() }
    }

    fun startFast() {
        viewModelScope.launch {
            val preferences = preferences.value
            val today = java.time.LocalDate.now()
            fastingRepository.start(
                plannedHours = preferences.fastingHoursFor(today),
                relaxed = preferences.relaxedDate == today.toString(),
            )
        }
    }

    fun finishFast() {
        activeSession.value?.let { session ->
            viewModelScope.launch { fastingRepository.finish(session.id) }
        }
    }
}