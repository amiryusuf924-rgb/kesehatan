package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_log WHERE loggedAtEpochMillis >= :start AND loggedAtEpochMillis < :end ORDER BY loggedAtEpochMillis DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_log WHERE loggedAtEpochMillis >= :start AND loggedAtEpochMillis < :end ORDER BY loggedAtEpochMillis")
    fun observeRange(start: Long, end: Long): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_log WHERE loggedAtEpochMillis >= :start AND loggedAtEpochMillis < :end ORDER BY loggedAtEpochMillis")
    suspend fun getBetween(start: Long, end: Long): List<MealLogEntity>

    @Query("SELECT * FROM meal_log ORDER BY loggedAtEpochMillis")
    suspend fun getAll(): List<MealLogEntity>

    @Insert
    suspend fun insert(log: MealLogEntity)

    @Insert
    suspend fun insertAll(logs: List<MealLogEntity>)

    @Query("DELETE FROM meal_log WHERE id = :id")
    suspend fun delete(id: Long)
}