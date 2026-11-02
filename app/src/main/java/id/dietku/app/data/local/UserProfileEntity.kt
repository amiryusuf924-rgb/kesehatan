package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val gender: String = "Laki-laki",
    val ageYears: Int = 27,
    val heightCm: Double = 165.0,
    val startingWeightKg: Double = 86.0,
    val currentWeightKg: Double = 86.0,
    @ColumnInfo(defaultValue = "81.7") val nextStageTargetWeightKg: Double = 81.7,
    val activityFactor: Double = 1.2,
    val calorieTargetKcal: Int = 1600,
    val proteinMinimumGrams: Int = 70,
    val proteinMaximumGrams: Int = 90,
)