package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "water_log", indices = [Index(value = ["loggedAtEpochMillis"])])
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val loggedAtEpochMillis: Long,
)