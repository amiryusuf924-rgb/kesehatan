package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_plan",
    indices = [Index(value = ["planDateEpochDay", "mealSlot"], unique = true)],
)
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planDateEpochDay: Long,
    val mealSlot: String,
    val menuKey: String,
    val menuName: String,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val proteinFamily: String,
    val ingredientsJson: String,
)