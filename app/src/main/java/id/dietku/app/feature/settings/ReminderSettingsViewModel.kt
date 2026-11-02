package id.dietku.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.ReminderRepository
import id.dietku.app.data.SettingsRepository
import id.dietku.app.data.local.ReminderSettingEntity
import id.dietku.app.reminders.ReminderScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val settingsRepository: SettingsRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {
    val reminders = reminderRepository.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val quietHours = settingsRepository.quietHours.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        8 to 16,
    )

    init {
        viewModelScope.launch {
            reminderRepository.ensureDefaults()
            scheduler.scheduleAll(reminderRepository.getAll())
        }
    }

    fun save(setting: ReminderSettingEntity) {
        viewModelScope.launch {
            reminderRepository.save(setting)
            scheduler.schedule(setting)
        }
    }

    fun setQuietHours(start: Int, end: Int) {
        viewModelScope.launch { settingsRepository.setQuietHours(start, end) }
    }
}