package id.dietku.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import id.dietku.app.data.SettingsRepository
import id.dietku.app.data.local.ReminderSettingEntity
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (!settingsRepository.isQuietNow()) ReminderNotifications.show(context, type)
                scheduler.scheduleNext(
                    ReminderSettingEntity(
                        reminderType = type,
                        enabled = true,
                        hour = intent.getIntExtra(ReminderScheduler.EXTRA_HOUR, 22),
                        minute = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTE, 0),
                        intervalMinutes = intent.getIntExtra(ReminderScheduler.EXTRA_INTERVAL, 0),
                    ),
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}