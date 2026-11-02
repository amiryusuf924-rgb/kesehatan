package id.dietku.app.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import id.dietku.app.data.local.ExerciseLogDao
import id.dietku.app.data.local.FastingLogDao
import id.dietku.app.data.local.MealLogDao
import id.dietku.app.data.local.MealPlanDao
import id.dietku.app.data.local.ReminderSettingDao
import id.dietku.app.data.local.ShoppingItemDao
import id.dietku.app.data.local.UserProfileDao
import id.dietku.app.data.local.WaterLogDao
import id.dietku.app.data.local.WeightLogDao
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class CsvExportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mealLogDao: MealLogDao,
    private val fastingLogDao: FastingLogDao,
    private val waterLogDao: WaterLogDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val weightLogDao: WeightLogDao,
    private val userProfileDao: UserProfileDao,
    private val mealPlanDao: MealPlanDao,
    private val shoppingItemDao: ShoppingItemDao,
    private val reminderSettingDao: ReminderSettingDao,
) {
    suspend fun exportAll(): Uri = withContext(Dispatchers.IO) {
        val rows = mutableListOf(
            listOf("jenis", "waktu", "nama", "jumlah", "kalori_kkal", "protein_g", "catatan"),
        )
        userProfileDao.getProfile()?.let { profile ->
            rows += listOf(
                "profil", "", profile.gender, "usia=${profile.ageYears};tinggi=${profile.heightCm} cm",
                profile.calorieTargetKcal.toString(), "${profile.proteinMinimumGrams}-${profile.proteinMaximumGrams}",
                "awal=${profile.startingWeightKg} kg;saat_ini=${profile.currentWeightKg} kg;target=${profile.nextStageTargetWeightKg} kg",
            )
        }
        mealLogDao.getAll().forEach { log ->
            rows += listOf(
                "makan", timestamp(log.loggedAtEpochMillis), log.foodName, log.quantity.toString(),
                log.caloriesKcal.toString(), log.proteinGrams.toString(), log.mealType,
            )
        }
        fastingLogDao.getAll().forEach { log ->
            rows += listOf(
                "puasa", timestamp(log.startEpochMillis), "Sesi puasa", "${log.plannedHours} jam",
                "", "", "selesai=${log.endEpochMillis?.let(::timestamp) ?: "belum"};longgar=${log.relaxed}",
            )
        }
        waterLogDao.getAll().forEach { log ->
            rows += listOf("air", timestamp(log.loggedAtEpochMillis), "Air putih", "${log.amountMl} ml", "", "", "")
        }
        exerciseLogDao.getAll().forEach { log ->
            rows += listOf(
                "olahraga", timestamp(log.loggedAtEpochMillis), log.activityName,
                "${log.durationMinutes} menit", "", "", "selesai=${log.completed}",
            )
        }
        weightLogDao.getAll().forEach { log ->
            rows += listOf("berat", timestamp(log.measuredAtEpochMillis), "Berat badan", "${log.weightKg} kg", "", "", "")
        }
        mealPlanDao.getAll().forEach { plan ->
            rows += listOf(
                "rencana_menu", LocalDate.ofEpochDay(plan.planDateEpochDay).toString(), plan.menuName,
                plan.mealSlot, plan.caloriesKcal.toString(), plan.proteinGrams.toString(), plan.ingredientsJson,
            )
        }
        shoppingItemDao.getAll().forEach { item ->
            rows += listOf(
                "belanja", LocalDate.ofEpochDay(item.weekStartEpochDay).toString(), item.name,
                "", "", "", "sudah_dibeli=${item.isChecked}",
            )
        }
        reminderSettingDao.getAll().forEach { setting ->
            rows += listOf(
                "pengingat", "${setting.hour.toString().padStart(2, '0')}:${setting.minute.toString().padStart(2, '0')}",
                setting.reminderType, "interval=${setting.intervalMinutes} menit", "", "", "aktif=${setting.enabled}",
            )
        }

        val exportDirectory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDirectory, "dietku-${System.currentTimeMillis()}.csv")
        file.writeText(rows.joinToString("\n") { row -> row.joinToString(",") { escape(it) } }, Charsets.UTF_8)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun timestamp(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toString()

    private fun escape(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}