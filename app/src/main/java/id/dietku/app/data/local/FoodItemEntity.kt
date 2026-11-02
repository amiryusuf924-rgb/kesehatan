package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_item")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val servingDescription: String,
    val caloriesKcal: Double,
    val proteinGrams: Double,
    val carbohydrateGrams: Double,
    val fatGrams: Double,
    val category: String,
    val isCustom: Boolean = false,
    val isFrequent: Boolean = false,
)