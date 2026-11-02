package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderSettingDao {
    @Query("SELECT * FROM reminder_setting ORDER BY reminderType")
    fun observeAll(): Flow<List<ReminderSettingEntity>>

    @Query("SELECT * FROM reminder_setting ORDER BY reminderType")
    suspend fun getAll(): List<ReminderSettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(setting: ReminderSettingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(settings: List<ReminderSettingEntity>)
}