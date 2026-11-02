package id.dietku.app.data

import id.dietku.app.data.local.FastingLogDao
import id.dietku.app.data.local.FastingLogEntity
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow

@Singleton
class FastingRepository @Inject constructor(
    private val fastingLogDao: FastingLogDao,
) {
    private val startMutex = Mutex()
    fun observeActive(): Flow<FastingLogEntity?> = fastingLogDao.observeActive()

    fun observeRecent(date: LocalDate = LocalDate.now()): Flow<List<FastingLogEntity>> {
        val zone = ZoneId.systemDefault()
        val start = date.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return fastingLogDao.observeBetween(start, end)
    }

    suspend fun start(plannedHours: Int, relaxed: Boolean) {
        startMutex.withLock {
            if (fastingLogDao.getActive() == null) {
                fastingLogDao.insert(
                    FastingLogEntity(
                        startEpochMillis = System.currentTimeMillis(),
                        plannedHours = plannedHours,
                        relaxed = relaxed,
                    ),
                )
            }
        }
    }

    suspend fun finish(id: Long) = fastingLogDao.finish(id, System.currentTimeMillis())
}