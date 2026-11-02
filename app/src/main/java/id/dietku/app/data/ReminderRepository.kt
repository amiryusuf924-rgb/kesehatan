package id.dietku.app.data

import id.dietku.app.data.local.ReminderSettingDao
import id.dietku.app.data.local.ReminderSettingEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ReminderRepository @Inject constructor(
    private val dao: ReminderSettingDao,
) {
    fun observeAll(): Flow<List<ReminderSettingEntity>> = dao.observeAll()

    suspend fun getAll(): List<ReminderSettingEntity> = dao.getAll()

    suspend fun ensureDefaults() {
        if (dao.getAll().isEmpty()) {
            dao.insertDefaults(
                listOf(
                    ReminderSettingEntity("meal_start", true, 11, 0),
                    ReminderSettingEntity("window_close", true, 18, 30),
                    ReminderSettingEntity("fast_start", true, 19, 0),
                    ReminderSettingEntity("water", true, 7, 0, intervalMinutes = 60),
                    ReminderSettingEntity("exercise", true, 7, 0),
                    ReminderSettingEntity("weight", true, 7, 0),
                    ReminderSettingEntity("meal_plan", true, 18, 0),
                    ReminderSettingEntity("sleep", true, 22, 0),
                ),
            )
        }
    }

    suspend fun save(setting: ReminderSettingEntity) = dao.save(setting)
}