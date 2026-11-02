package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plan WHERE planDateEpochDay >= :start AND planDateEpochDay < :end ORDER BY planDateEpochDay, mealSlot")
    fun observeWeek(start: Long, end: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan WHERE planDateEpochDay >= :start AND planDateEpochDay < :end ORDER BY planDateEpochDay, mealSlot")
    suspend fun getWeek(start: Long, end: Long): List<MealPlanEntity>

    @Query("SELECT * FROM meal_plan ORDER BY planDateEpochDay, mealSlot")
    suspend fun getAll(): List<MealPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlans(plans: List<MealPlanEntity>)

    @Query("DELETE FROM meal_plan WHERE planDateEpochDay >= :start AND planDateEpochDay < :end")
    suspend fun clearWeek(start: Long, end: Long)
}