package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_log WHERE loggedAtEpochMillis >= :start AND loggedAtEpochMillis < :end ORDER BY loggedAtEpochMillis DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_log ORDER BY loggedAtEpochMillis")
    suspend fun getAll(): List<WaterLogEntity>

    @Insert
    suspend fun insert(log: WaterLogEntity)
}