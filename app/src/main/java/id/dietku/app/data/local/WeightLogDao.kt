package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_log ORDER BY measuredAtEpochMillis DESC")
    fun observeAll(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_log ORDER BY measuredAtEpochMillis")
    suspend fun getAll(): List<WeightLogEntity>

    @Insert
    suspend fun insert(log: WeightLogEntity)
}