package id.dietku.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.dietku.app.domain.HealthCalculations
import id.dietku.app.domain.FastingWindowCalculator
import id.dietku.app.feature.home.DashboardViewModel
import id.dietku.app.feature.profile.ProfileViewModel
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val meals by dashboardViewModel.meals.collectAsStateWithLifecycle()
    val water by dashboardViewModel.water.collectAsStateWithLifecycle()
    val exercise by dashboardViewModel.exercise.collectAsStateWithLifecycle()
    val fastingPreferences by dashboardViewModel.fastingPreferences.collectAsStateWithLifecycle()
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    var showWeightDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = ZonedDateTime.now()
        }
    }

    val calories = meals.sumOf { it.caloriesKcal }
    val protein = meals.sumOf { it.proteinGrams }
    val calorieTarget = profile?.calorieTargetKcal ?: 1600
    val waterMl = water.sumOf { it.amountMl }
    val fastingHours = fastingPreferences.fastingHoursFor(now.toLocalDate())
    val fastingState = FastingWindowCalculator.calculate(
        now = now,
        eatingStart = LocalTime.of(fastingPreferences.eatingStartHour, 0),
        fastingHours = fastingHours,
    )
    val remaining = Duration.between(now, fastingState.nextChange).coerceAtLeast(Duration.ZERO)
    val workoutDone = exercise.any { it.completed }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Hari ini", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "${now.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("id", "ID"))}, ${now.toLocalDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            if (fastingState.isEatingWindow) "Jendela makan sedang terbuka" else "Sedang dalam waktu puasa",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text("${formatDuration(remaining)} lagi ${if (fastingState.isEatingWindow) "menuju mulai puasa" else "menuju jendela makan"}")
                        Text(
                            "${fastingPreferences.eatingStartHour.toString().padStart(2, '0')}.00  •  " +
                                "${fastingHours}:${24 - fastingHours}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Asupan hari ini", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kalori")
                            Text("${calories.toInt()} / $calorieTarget kkal")
                        }
                        LinearProgressIndicator(
                            progress = { (calories / calorieTarget).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Protein")
                            Text("${protein.toInt()} / ${profile?.proteinMinimumGrams ?: 70}-${profile?.proteinMaximumGrams ?: 90} g")
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        title = "Air putih",
                        value = "${waterMl / 250} / 8 gelas",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCard(
                        title = "Olahraga",
                        value = if (workoutDone) "Selesai" else "Belum",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Profil & berat", style = MaterialTheme.typography.titleMedium)
                        if (profile != null) {
                            val bmi = HealthCalculations.bmi(profile!!.currentWeightKg, profile!!.heightCm)
                            Text("${format(profile!!.currentWeightKg)} kg  •  BMI ${format(bmi)}")
                            Text("${HealthCalculations.whoBmiCategory(bmi)}  •  target ${profile!!.calorieTargetKcal} kkal")
                        } else {
                            Text("Menyiapkan profil…")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Button(
                onClick = { onNavigate("food") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null)
                Text("  Catat makan")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = { dashboardViewModel.addWater() },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.WaterDrop, contentDescription = null)
                    Text(" Air")
                }
                FilledTonalButton(
                    onClick = dashboardViewModel::completeTodayWorkout,
                    enabled = !workoutDone,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                    Text(" Olahraga")
                }
                FilledTonalButton(
                    onClick = { showWeightDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.MonitorWeight, contentDescription = null)
                    Text(" Berat")
                }
            }
        }
    }

    if (showWeightDialog) {
        QuickWeightDialog(
            onDismiss = { showWeightDialog = false },
            onSave = { weight ->
                dashboardViewModel.logWeight(weight)
                showWeightDialog = false
            },
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun QuickWeightDialog(onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var value by remember { mutableStateOf("") }
    val parsed = value.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat berat") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Berat dalam kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { parsed?.let(onSave) }, enabled = parsed != null && parsed > 0) {
                Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

private fun formatDuration(duration: Duration): String = String.format(
    Locale.ROOT,
    "%02d:%02d:%02d",
    duration.toHours(),
    duration.toMinutes() % 60,
    duration.seconds % 60,
)

private fun format(value: Double): String = String.format(Locale("id", "ID"), "%.1f", value)