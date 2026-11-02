package id.dietku.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import id.dietku.app.data.local.ReminderSettingEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun schedule(setting: ReminderSettingEntity) {
        if (!setting.enabled) {
            cancel(setting.reminderType)
            return
        }
        if (setting.reminderType == "water") {
            val interval = setting.intervalMinutes.coerceAtLeast(15)
            val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(interval.toLong(), TimeUnit.MINUTES)
                .setInitialDelay(interval.toLong(), TimeUnit.MINUTES)
                .setInputData(
                    androidx.work.workDataOf(
                        WaterReminderWorker.START_HOUR to setting.hour,
                        WaterReminderWorker.END_HOUR to 21,
                    ),
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WATER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        } else {
            scheduleNext(setting)
        }
    }

    fun scheduleAll(settings: List<ReminderSettingEntity>) {
        settings.forEach(::schedule)
    }

    fun scheduleNext(setting: ReminderSettingEntity) {
        if (!setting.enabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, setting.hour)
            set(Calendar.MINUTE, setting.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (setting.reminderType == "weight" || setting.reminderType == "meal_plan") {
            while (next.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY || !next.after(now)) {
                next.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else if (!next.after(now)) {
            next.add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "id.dietku.app.REMINDER.${setting.reminderType}"
            putExtra(EXTRA_TYPE, setting.reminderType)
            putExtra(EXTRA_HOUR, setting.hour)
            putExtra(EXTRA_MINUTE, setting.minute)
            putExtra(EXTRA_INTERVAL, setting.intervalMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            setting.reminderType.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
        }
    }

    fun cancel(type: String) {
        if (type == "water") {
            WorkManager.getInstance(context).cancelUniqueWork(WATER_WORK_NAME)
            return
        }
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "id.dietku.app.REMINDER.$type"
        }
        PendingIntent.getBroadcast(
            context,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ).cancel()
    }

    companion object {
        const val EXTRA_TYPE = "reminder_type"
        const val EXTRA_HOUR = "reminder_hour"
        const val EXTRA_MINUTE = "reminder_minute"
        const val EXTRA_INTERVAL = "reminder_interval"
        private const val WATER_WORK_NAME = "dietku_water_reminder"
    }
}