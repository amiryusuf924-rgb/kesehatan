package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_log", indices = [Index(value = ["startEpochMillis"])])
data class FastingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startEpochMillis: Long,
    val endEpochMillis: Long? = null,
    val plannedHours: Int,
    val relaxed: Boolean = false,
)