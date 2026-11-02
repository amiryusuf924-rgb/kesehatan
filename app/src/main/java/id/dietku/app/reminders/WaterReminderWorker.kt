package id.dietku.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.dietku.app.data.SettingsRepository
import java.time.LocalTime

class WaterReminderWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val now = LocalTime.now()
        val startHour = inputData.getInt(START_HOUR, 7)
        val endHour = inputData.getInt(END_HOUR, 21)
        if (now.hour >= startHour && now.hour < endHour && !SettingsRepository(applicationContext).isQuietNow(now)) {
            ReminderNotifications.show(applicationContext, "water")
        }
        return Result.success()
    }

    companion object {
        const val START_HOUR = "active_start_hour"
        const val END_HOUR = "active_end_hour"
    }
}