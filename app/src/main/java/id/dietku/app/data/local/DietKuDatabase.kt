package id.dietku.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        FoodItemEntity::class,
        MealLogEntity::class,
        FastingLogEntity::class,
        WaterLogEntity::class,
        ExerciseLogEntity::class,
        WeightLogEntity::class,
        MealPlanEntity::class,
        ShoppingItemEntity::class,
        ReminderSettingEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class DietKuDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun fastingLogDao(): FastingLogDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun reminderSettingDao(): ReminderSettingDao
}