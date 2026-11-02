package id.dietku.app.data

import id.dietku.app.data.local.MealPlanDao
import id.dietku.app.data.local.MealPlanEntity
import id.dietku.app.data.local.ShoppingItemDao
import id.dietku.app.data.local.ShoppingItemEntity
import id.dietku.app.domain.MenuCatalog
import id.dietku.app.domain.MenuPreset
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

@Singleton
class MealPlanRepository @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val shoppingItemDao: ShoppingItemDao,
) {
    fun observeWeek(date: LocalDate = LocalDate.now()): Flow<List<MealPlanEntity>> =
        mealPlanDao.observeWeek(weekStart(date).toEpochDay(), weekStart(date).plusDays(7).toEpochDay())

    fun observeShopping(date: LocalDate = LocalDate.now()): Flow<List<ShoppingItemEntity>> =
        shoppingItemDao.observeWeek(weekStart(date).toEpochDay())

    suspend fun ensureWeek(date: LocalDate = LocalDate.now()) {
        val start = weekStart(date)
        val end = start.plusDays(7)
        val existing = mealPlanDao.getWeek(start.toEpochDay(), end.toEpochDay())
            .map { it.planDateEpochDay to it.mealSlot }
            .toSet()
        val plans = (0..6).flatMap { dayOffset ->
            val day = start.plusDays(dayOffset.toLong())
            listOf("Makan 1", "Makan 2").mapIndexedNotNull { slotIndex, slot ->
                if (day.toEpochDay() to slot in existing) null else {
                    val options = MenuCatalog.forSlot(slot)
                    toEntity(day, options[(dayOffset + slotIndex) % options.size])
                }
            }
        }
        if (plans.isNotEmpty()) mealPlanDao.savePlans(plans)
    }

    suspend fun assign(date: LocalDate, preset: MenuPreset) {
        mealPlanDao.savePlans(listOf(toEntity(date, preset)))
    }

    suspend fun randomizeWeek(date: LocalDate = LocalDate.now()) {
        val start = weekStart(date)
        val random = kotlin.random.Random(System.currentTimeMillis())
        var previousFamily = ""
        val plans = (0..6).flatMap { dayOffset ->
            val day = start.plusDays(dayOffset.toLong())
            val firstOptions = MenuCatalog.forSlot("Makan 1").filter { it.proteinFamily != previousFamily }
            val first = firstOptions.random(random)
            val secondOptions = MenuCatalog.forSlot("Makan 2").filter { it.proteinFamily != first.proteinFamily }
            val second = secondOptions.random(random)
            previousFamily = second.proteinFamily
            listOf(toEntity(day, first), toEntity(day, second))
        }
        mealPlanDao.clearWeek(start.toEpochDay(), start.plusDays(7).toEpochDay())
        mealPlanDao.savePlans(plans)
    }

    suspend fun buildShoppingList(date: LocalDate = LocalDate.now()) {
        val start = weekStart(date)
        val plans = mealPlanDao.getWeek(start.toEpochDay(), start.plusDays(7).toEpochDay())
        val ingredientNames = plans.flatMap { plan ->
            val array = JSONArray(plan.ingredientsJson)
            (0 until array.length()).map(array::getString)
        }.distinctBy { it.lowercase() }.sorted()
        shoppingItemDao.clearWeek(start.toEpochDay())
        shoppingItemDao.insertAll(
            ingredientNames.map { name ->
                ShoppingItemEntity(weekStartEpochDay = start.toEpochDay(), name = name)
            },
        )
    }

    suspend fun updateShoppingItem(item: ShoppingItemEntity) = shoppingItemDao.update(item)

    private fun toEntity(date: LocalDate, preset: MenuPreset) = MealPlanEntity(
        planDateEpochDay = date.toEpochDay(),
        mealSlot = preset.slot,
        menuKey = preset.key,
        menuName = preset.name,
        caloriesKcal = preset.caloriesKcal,
        proteinGrams = preset.proteinGrams,
        proteinFamily = preset.proteinFamily,
        ingredientsJson = JSONArray(preset.ingredients).toString(),
    )

    private fun weekStart(date: LocalDate) = date.with(DayOfWeek.MONDAY)
}