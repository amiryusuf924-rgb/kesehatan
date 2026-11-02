package id.dietku.app.di

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.dietku.app.data.local.DietKuDatabase
import id.dietku.app.data.local.FoodCatalog
import id.dietku.app.data.local.FoodItemDao
import id.dietku.app.data.local.ExerciseLogDao
import id.dietku.app.data.local.FastingLogDao
import id.dietku.app.data.local.MealLogDao
import id.dietku.app.data.local.UserProfileDao
import id.dietku.app.data.local.WaterLogDao
import id.dietku.app.data.local.WeightLogDao
import id.dietku.app.data.local.MealPlanDao
import id.dietku.app.data.local.ShoppingItemDao
import id.dietku.app.data.local.ReminderSettingDao
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `food_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `servingDescription` TEXT NOT NULL, `caloriesKcal` REAL NOT NULL, `proteinGrams` REAL NOT NULL, `carbohydrateGrams` REAL NOT NULL, `fatGrams` REAL NOT NULL, `category` TEXT NOT NULL, `isCustom` INTEGER NOT NULL, `isFrequent` INTEGER NOT NULL)""",
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `meal_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `foodItemId` INTEGER NOT NULL, `foodName` TEXT NOT NULL, `servingDescription` TEXT NOT NULL, `quantity` REAL NOT NULL, `caloriesKcal` REAL NOT NULL, `proteinGrams` REAL NOT NULL, `carbohydrateGrams` REAL NOT NULL, `fatGrams` REAL NOT NULL, `mealType` TEXT NOT NULL, `loggedAtEpochMillis` INTEGER NOT NULL)""",
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_meal_log_loggedAtEpochMillis` ON `meal_log` (`loggedAtEpochMillis`)",
            )
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `fasting_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startEpochMillis` INTEGER NOT NULL, `endEpochMillis` INTEGER, `plannedHours` INTEGER NOT NULL, `relaxed` INTEGER NOT NULL)""",
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_fasting_log_startEpochMillis` ON `fasting_log` (`startEpochMillis`)",
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `water_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountMl` INTEGER NOT NULL, `loggedAtEpochMillis` INTEGER NOT NULL)""",
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_water_log_loggedAtEpochMillis` ON `water_log` (`loggedAtEpochMillis`)",
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `exercise_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `activityName` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `loggedAtEpochMillis` INTEGER NOT NULL)""",
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_exercise_log_loggedAtEpochMillis` ON `exercise_log` (`loggedAtEpochMillis`)",
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `weight_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `weightKg` REAL NOT NULL, `measuredAtEpochMillis` INTEGER NOT NULL)""",
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_weight_log_measuredAtEpochMillis` ON `weight_log` (`measuredAtEpochMillis`)",
            )
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `meal_plan` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `planDateEpochDay` INTEGER NOT NULL, `mealSlot` TEXT NOT NULL, `menuKey` TEXT NOT NULL, `menuName` TEXT NOT NULL, `caloriesKcal` INTEGER NOT NULL, `proteinGrams` REAL NOT NULL, `proteinFamily` TEXT NOT NULL, `ingredientsJson` TEXT NOT NULL)""",
            )
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_meal_plan_planDateEpochDay_mealSlot` ON `meal_plan` (`planDateEpochDay`, `mealSlot`)",
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `shopping_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `weekStartEpochDay` INTEGER NOT NULL, `name` TEXT NOT NULL, `isChecked` INTEGER NOT NULL)""",
            )
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_shopping_item_weekStartEpochDay_name` ON `shopping_item` (`weekStartEpochDay`, `name`)",
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `reminder_setting` (`reminderType` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `intervalMinutes` INTEGER NOT NULL, PRIMARY KEY(`reminderType`))""",
            )
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE `user_profile` ADD COLUMN `nextStageTargetWeightKg` REAL NOT NULL DEFAULT 81.7",
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DietKuDatabase {
        lateinit var database: DietKuDatabase
        database = Room.databaseBuilder(context, DietKuDatabase::class.java, "dietku.db")
            .addMigrations(migration1To2)
            .addMigrations(migration2To3)
            .addMigrations(migration3To4)
            .addMigrations(migration4To5)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        if (database.foodItemDao().count() == 0) {
                            database.foodItemDao().insertAll(FoodCatalog.initialItems)
                        }
                    }
                }
            })
            .build()
        return database
    }

    @Provides
    fun provideUserProfileDao(database: DietKuDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideFoodItemDao(database: DietKuDatabase): FoodItemDao = database.foodItemDao()

    @Provides
    fun provideMealLogDao(database: DietKuDatabase): MealLogDao = database.mealLogDao()

    @Provides
    fun provideFastingLogDao(database: DietKuDatabase): FastingLogDao = database.fastingLogDao()

    @Provides
    fun provideWaterLogDao(database: DietKuDatabase): WaterLogDao = database.waterLogDao()

    @Provides
    fun provideExerciseLogDao(database: DietKuDatabase): ExerciseLogDao = database.exerciseLogDao()

    @Provides
    fun provideWeightLogDao(database: DietKuDatabase): WeightLogDao = database.weightLogDao()

    @Provides
    fun provideMealPlanDao(database: DietKuDatabase): MealPlanDao = database.mealPlanDao()

    @Provides
    fun provideShoppingItemDao(database: DietKuDatabase): ShoppingItemDao = database.shoppingItemDao()

    @Provides
    fun provideReminderSettingDao(database: DietKuDatabase): ReminderSettingDao = database.reminderSettingDao()
}