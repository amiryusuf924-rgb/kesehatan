package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingLogDao {
    @Query("SELECT * FROM fasting_log WHERE startEpochMillis >= :start AND startEpochMillis < :end ORDER BY startEpochMillis DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<FastingLogEntity>>

    @Query("SELECT * FROM fasting_log WHERE endEpochMillis IS NULL ORDER BY startEpochMillis DESC LIMIT 1")
    fun observeActive(): Flow<FastingLogEntity?>

    @Query("SELECT * FROM fasting_log WHERE endEpochMillis IS NULL ORDER BY startEpochMillis DESC LIMIT 1")
    suspend fun getActive(): FastingLogEntity?

    @Query("SELECT * FROM fasting_log ORDER BY startEpochMillis")
    suspend fun getAll(): List<FastingLogEntity>

    @Insert
    suspend fun insert(log: FastingLogEntity): Long

    @Query("UPDATE fasting_log SET endEpochMillis = :end WHERE id = :id AND endEpochMillis IS NULL")
    suspend fun finish(id: Long, end: Long)
}