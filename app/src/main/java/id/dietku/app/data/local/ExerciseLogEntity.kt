package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_log", indices = [Index(value = ["loggedAtEpochMillis"])])
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityName: String,
    val durationMinutes: Int,
    val completed: Boolean,
    val loggedAtEpochMillis: Long,
)