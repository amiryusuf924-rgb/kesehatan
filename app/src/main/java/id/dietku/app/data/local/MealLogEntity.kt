package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meal_log", indices = [Index(value = ["loggedAtEpochMillis"])])
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodItemId: Long,
    val foodName: String,
    val servingDescription: String,
    val quantity: Double,
    val caloriesKcal: Double,
    val proteinGrams: Double,
    val carbohydrateGrams: Double,
    val fatGrams: Double,
    val mealType: String,
    val loggedAtEpochMillis: Long,
)