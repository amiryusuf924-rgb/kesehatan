package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_setting")
data class ReminderSettingEntity(
    @PrimaryKey val reminderType: String,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val intervalMinutes: Int = 0,
)