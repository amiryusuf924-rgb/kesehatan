package id.dietku.app.data

import id.dietku.app.data.local.ExerciseLogDao
import id.dietku.app.data.local.ExerciseLogEntity
import id.dietku.app.data.local.WaterLogDao
import id.dietku.app.data.local.WaterLogEntity
import id.dietku.app.data.local.WeightLogDao
import id.dietku.app.data.local.WeightLogEntity
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DailyTrackingRepository @Inject constructor(
    private val waterLogDao: WaterLogDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val weightLogDao: WeightLogDao,
) {
    fun observeWater(date: LocalDate = LocalDate.now()): Flow<List<WaterLogEntity>> {
        val (start, end) = dateBounds(date)
        return waterLogDao.observeBetween(start, end)
    }

    fun observeExercise(date: LocalDate = LocalDate.now()): Flow<List<ExerciseLogEntity>> {
        val (start, end) = dateBounds(date)
        return exerciseLogDao.observeBetween(start, end)
    }

    fun observeExerciseRange(startDate: LocalDate, endDateExclusive: LocalDate): Flow<List<ExerciseLogEntity>> {
        val zone = ZoneId.systemDefault()
        val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = endDateExclusive.atStartOfDay(zone).toInstant().toEpochMilli()
        return exerciseLogDao.observeRange(start, end)
    }

    fun observeWeights(): Flow<List<WeightLogEntity>> = weightLogDao.observeAll()

    suspend fun addWater(amountMl: Int) {
        require(amountMl > 0)
        waterLogDao.insert(WaterLogEntity(amountMl = amountMl, loggedAtEpochMillis = System.currentTimeMillis()))
    }

    suspend fun logExercise(
        name: String,
        durationMinutes: Int,
        loggedAtEpochMillis: Long = System.currentTimeMillis(),
    ) {
        require(durationMinutes > 0)
        exerciseLogDao.insert(
            ExerciseLogEntity(
                activityName = name,
                durationMinutes = durationMinutes,
                completed = true,
                loggedAtEpochMillis = loggedAtEpochMillis,
            ),
        )
    }

    suspend fun deleteExercise(id: Long) = exerciseLogDao.delete(id)

    suspend fun logWeight(weightKg: Double, measuredAtEpochMillis: Long = System.currentTimeMillis()) {
        require(weightKg > 0.0)
        weightLogDao.insert(WeightLogEntity(weightKg = weightKg, measuredAtEpochMillis = measuredAtEpochMillis))
    }

    private fun dateBounds(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        return date.atStartOfDay(zone).toInstant().toEpochMilli() to
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }
}