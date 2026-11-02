package id.dietku.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.dietku.app.data.local.UserProfileEntity
import id.dietku.app.domain.HealthCalculations
import id.dietku.app.feature.profile.ProfileViewModel
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    if (profile == null) {
        Text("Menyiapkan profil…", modifier = Modifier.padding(24.dp))
        return
    }

    val savedProfile = profile!!
    var gender by remember(savedProfile) { mutableStateOf(savedProfile.gender) }
    var age by remember(savedProfile) { mutableStateOf(savedProfile.ageYears.toString()) }
    var height by remember(savedProfile) { mutableStateOf(savedProfile.heightCm.toString()) }
    var startingWeight by remember(savedProfile) { mutableStateOf(savedProfile.startingWeightKg.toString()) }
    var currentWeight by remember(savedProfile) { mutableStateOf(savedProfile.currentWeightKg.toString()) }
    var nextStageTarget by remember(savedProfile) { mutableStateOf(savedProfile.nextStageTargetWeightKg.toString()) }
    var activityFactor by remember(savedProfile) { mutableStateOf(savedProfile.activityFactor) }
    var calorieTarget by remember(savedProfile) { mutableStateOf(savedProfile.calorieTargetKcal.toString()) }
    var proteinMinimum by remember(savedProfile) { mutableStateOf(savedProfile.proteinMinimumGrams.toString()) }
    var proteinMaximum by remember(savedProfile) { mutableStateOf(savedProfile.proteinMaximumGrams.toString()) }
    var pendingProfile by remember { mutableStateOf<UserProfileEntity?>(null) }

    val parsedAge = age.toIntOrNull()
    val parsedHeight = height.toDoubleOrNull()
    val parsedStartingWeight = startingWeight.toDoubleOrNull()
    val parsedCurrentWeight = currentWeight.toDoubleOrNull()
    val parsedNextStageTarget = nextStageTarget.toDoubleOrNull()
    val parsedTarget = calorieTarget.toIntOrNull()
    val parsedProteinMinimum = proteinMinimum.toIntOrNull()
    val parsedProteinMaximum = proteinMaximum.toIntOrNull()
    val valid = parsedAge != null && parsedAge > 0 &&
        parsedHeight != null && parsedHeight > 0.0 &&
        parsedStartingWeight != null && parsedStartingWeight > 0.0 &&
        parsedCurrentWeight != null && parsedCurrentWeight > 0.0 &&
        parsedNextStageTarget != null && parsedNextStageTarget > 0.0 &&
        parsedTarget != null && parsedTarget > 0 &&
        parsedProteinMinimum != null && parsedProteinMinimum >= 0 &&
        parsedProteinMaximum != null && parsedProteinMaximum >= parsedProteinMinimum

    val draft = if (valid) {
        savedProfile.copy(
            gender = gender,
            ageYears = parsedAge!!,
            heightCm = parsedHeight!!,
            startingWeightKg = parsedStartingWeight!!,
            currentWeightKg = parsedCurrentWeight!!,
            nextStageTargetWeightKg = parsedNextStageTarget!!,
            activityFactor = activityFactor,
            calorieTargetKcal = parsedTarget!!,
            proteinMinimumGrams = parsedProteinMinimum!!,
            proteinMaximumGrams = parsedProteinMaximum!!,
        )
    } else {
        null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Profil", style = MaterialTheme.typography.headlineSmall)
            Text("Informasi ini dipakai untuk menghitung kebutuhan harianmu.")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = gender == "Laki-laki",
                    onClick = { gender = "Laki-laki" },
                    label = { Text("Laki-laki") },
                )
                FilterChip(
                    selected = gender == "Perempuan",
                    onClick = { gender = "Perempuan" },
                    label = { Text("Perempuan") },
                )
            }

            NumberField("Usia", age, "tahun", KeyboardType.Number) { age = it }
            NumberField("Tinggi badan", height, "cm", KeyboardType.Decimal) { height = it }
            NumberField("Berat awal", startingWeight, "kg", KeyboardType.Decimal) { startingWeight = it }
            NumberField("Berat saat ini", currentWeight, "kg", KeyboardType.Decimal) { currentWeight = it }
            NumberField("Target berat tahap berikutnya", nextStageTarget, "kg", KeyboardType.Decimal) {
                nextStageTarget = it
            }

            Text("Aktivitas harian", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1.2 to "Ringan", 1.375 to "Sedang", 1.55 to "Aktif").forEach { (factor, label) ->
                    FilterChip(
                        selected = activityFactor == factor,
                        onClick = { activityFactor = factor },
                        label = { Text(label) },
                    )
                }
            }
            Text(
                "Pilih berdasarkan rutinitas harian, bukan hanya sesi olahraga.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NumberField("Target kalori", calorieTarget, "kkal/hari", KeyboardType.Number) {
                calorieTarget = it
            }
            NumberField("Protein minimum", proteinMinimum, "g/hari", KeyboardType.Number) {
                proteinMinimum = it
            }
            NumberField("Protein maksimum", proteinMaximum, "g/hari", KeyboardType.Number) {
                proteinMaximum = it
            }

            if (draft != null) {
                CalculationCard(draft)
            }
        }

        Button(
            onClick = {
                val value = draft ?: return@Button
                if (value.calorieTargetKcal < 1500) pendingProfile = value
                else viewModel.saveProfile(value)
            },
            enabled = valid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Simpan profil")
        }
    }

    pendingProfile?.let { profileToSave ->
        AlertDialog(
            onDismissRequest = { pendingProfile = null },
            title = { Text("Target kalori sangat rendah") },
            text = {
                Text(
                    "Target di bawah 1.500 kkal dapat berisiko bila tidak sesuai kebutuhanmu. " +
                        "Sebaiknya konsultasikan target ini dengan dokter atau ahli gizi. Tetap simpan?",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveProfile(profileToSave)
                    pendingProfile = null
                }) { Text("Simpan setelah paham") }
            },
            dismissButton = {
                TextButton(onClick = { pendingProfile = null }) { Text("Ubah target") }
            },
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    suffix: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CalculationCard(profile: UserProfileEntity) {
    val bmi = HealthCalculations.bmi(profile.currentWeightKg, profile.heightCm)
    val bmr = HealthCalculations.bmrMifflinStJeor(
        profile.currentWeightKg,
        profile.heightCm,
        profile.ageYears,
        profile.gender,
    )
    val dailyNeeds = HealthCalculations.dailyEnergyNeedsKcal(bmr, profile.activityFactor)
    val suggestedCalories = HealthCalculations.recommendedCalorieTargetKcal(dailyNeeds)
    val stageRange = HealthCalculations.stageOneWeightRange(profile.startingWeightKg)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Perkiraan", style = MaterialTheme.typography.titleMedium)
            Text("BMI ${format(bmi)}  •  WHO: ${HealthCalculations.whoBmiCategory(bmi)}")
            Text("Asia-Pasifik: ${HealthCalculations.asiaPacificBmiCategory(bmi)}")
            Text("BMR ${bmr.toInt()} kkal  •  kebutuhan harian ${dailyNeeds} kkal")
            Text("Saran target defisit 500 kkal: $suggestedCalories kkal/hari")
            Text(
                "Tahap 1 (turun 5-10%): ${format(stageRange.start)}-${format(stageRange.endInclusive)} kg",
            )
            Text("Target tahap berikutnya: ${format(profile.nextStageTargetWeightKg)} kg")
        }
    }
    Text(
        "Estimasi bersifat umum. Target protein awal: ${profile.proteinMinimumGrams}-${profile.proteinMaximumGrams} g/hari.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun format(value: Double): String = String.format(Locale("id", "ID"), "%.1f", value)