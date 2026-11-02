package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_log WHERE loggedAtEpochMillis >= :start AND loggedAtEpochMillis < :end ORDER BY loggedAtEpochMillis DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_log WHERE loggedAtEpochMillis >= :start AND loggedAtEpochMillis < :end ORDER BY loggedAtEpochMillis DESC")
    fun observeRange(start: Long, end: Long): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_log ORDER BY loggedAtEpochMillis")
    suspend fun getAll(): List<ExerciseLogEntity>

    @Insert
    suspend fun insert(log: ExerciseLogEntity)

    @Query("DELETE FROM exercise_log WHERE id = :id")
    suspend fun delete(id: Long)
}