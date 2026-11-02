package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_log", indices = [Index(value = ["measuredAtEpochMillis"])])
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Double,
    val measuredAtEpochMillis: Long,
)