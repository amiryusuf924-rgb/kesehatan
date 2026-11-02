package id.dietku.app.data

import id.dietku.app.data.local.FoodItemDao
import id.dietku.app.data.local.FoodItemEntity
import id.dietku.app.data.local.MealLogDao
import id.dietku.app.data.local.MealLogEntity
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class MealLogRepository @Inject constructor(
    private val foodItemDao: FoodItemDao,
    private val mealLogDao: MealLogDao,
) {
    fun observeFoods(): Flow<List<FoodItemEntity>> = foodItemDao.observeAll()

    fun observeDay(date: LocalDate): Flow<List<MealLogEntity>> {
        val (start, end) = dateBounds(date)
        return mealLogDao.observeBetween(start, end)
    }

    fun observeRange(startDate: LocalDate, endDateExclusive: LocalDate): Flow<List<MealLogEntity>> {
        val zone = ZoneId.systemDefault()
        val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = endDateExclusive.atStartOfDay(zone).toInstant().toEpochMilli()
        return mealLogDao.observeRange(start, end)
    }

    suspend fun addCustomFood(food: FoodItemEntity) = foodItemDao.insert(food)

    suspend fun logFood(food: FoodItemEntity, quantity: Double, mealType: String) {
        mealLogDao.insert(
            MealLogEntity(
                foodItemId = food.id,
                foodName = food.name,
                servingDescription = food.servingDescription,
                quantity = quantity,
                caloriesKcal = food.caloriesKcal * quantity,
                proteinGrams = food.proteinGrams * quantity,
                carbohydrateGrams = food.carbohydrateGrams * quantity,
                fatGrams = food.fatGrams * quantity,
                mealType = mealType,
                loggedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        foodItemDao.markFrequent(food.id)
    }

    suspend fun deleteLog(id: Long) = mealLogDao.delete(id)

    suspend fun copyYesterday(date: LocalDate = LocalDate.now()) {
        val previousDate = date.minusDays(1)
        val (previousStart, previousEnd) = dateBounds(previousDate)
        val (todayStart) = dateBounds(date)
        val copiedLogs = mealLogDao.getBetween(previousStart, previousEnd).map { oldLog ->
            oldLog.copy(
                id = 0,
                loggedAtEpochMillis = todayStart + (oldLog.loggedAtEpochMillis - previousStart),
            )
        }
        if (copiedLogs.isNotEmpty()) mealLogDao.insertAll(copiedLogs)
    }

    private fun dateBounds(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}