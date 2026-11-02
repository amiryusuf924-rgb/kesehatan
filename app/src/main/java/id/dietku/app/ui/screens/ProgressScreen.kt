package id.dietku.app.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import id.dietku.app.data.local.WeightLogEntity
import id.dietku.app.domain.HealthCalculations
import id.dietku.app.feature.progress.ProgressViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private data class ChartPoint(val label: String, val value: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val weights by viewModel.weights.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val mealLogs by viewModel.weeklyMeals.collectAsStateWithLifecycle()
    val fastingSessions by viewModel.fastingSessions.collectAsStateWithLifecycle()
    var weightInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val sortedWeights = weights.sortedBy { it.measuredAtEpochMillis }
    val latestWeight = sortedWeights.lastOrNull()
    val previousWeight = sortedWeights.getOrNull(sortedWeights.lastIndex - 1)
    val startingWeight = profile?.startingWeightKg ?: 86.0
    val currentWeight = latestWeight?.weightKg ?: profile?.currentWeightKg ?: startingWeight
    val bmi = profile?.let { HealthCalculations.bmi(currentWeight, it.heightCm) }
    val totalLoss = startingWeight - currentWeight
    val percentChange = (totalLoss / startingWeight) * 100.0

    val weightPoints = sortedWeights.map {
        ChartPoint(
            label = Instant.ofEpochMilli(it.measuredAtEpochMillis)
                .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d/M")),
            value = it.weightKg.toFloat(),
        )
    }
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val caloriesByDay = mealLogs.groupBy {
        Instant.ofEpochMilli(it.loggedAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val caloriePoints = days.map { date ->
        ChartPoint(date.dayOfMonth.toString(), (caloriesByDay[date]?.sumOf { it.caloriesKcal } ?: 0.0).toFloat())
    }
    val fastingPoints = days.map { date ->
        val daySession = fastingSessions.firstOrNull {
            Instant.ofEpochMilli(it.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == date
        }
        val progress = daySession?.let { session ->
            val end = session.endEpochMillis ?: System.currentTimeMillis()
            val hours = Duration.ofMillis((end - session.startEpochMillis).coerceAtLeast(0L)).toMinutes() / 60.0
            ((hours / session.plannedHours) * 100.0).coerceIn(0.0, 100.0).toFloat()
        } ?: 0f
        ChartPoint(date.dayOfMonth.toString(), progress)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kemajuan", style = MaterialTheme.typography.headlineSmall)
                Text("Timbang mingguan pada pagi hari sebelum makan.")
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${format(currentWeight)} kg", style = MaterialTheme.typography.headlineMedium)
                    Text("Perubahan total ${formatSigned(-totalLoss)} kg (${formatSigned(-percentChange)}%) dari awal")
                    if (previousWeight != null) {
                        Text("${formatSigned(currentWeight - previousWeight.weightKg)} kg sejak penimbangan sebelumnya")
                    }
                    if (bmi != null) Text("BMI ${format(bmi)}  •  ${HealthCalculations.whoBmiCategory(bmi)}")
                    if (latestWeight == null) {
                        Text("Angka awal dari profil. Catat penimbangan pertama untuk memulai grafik.")
                    }
                }
            }
        }
        item {
            WeightTrendMessage(weights = sortedWeights)
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Catat berat", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))))
                    }
                    Button(
                        onClick = {
                            viewModel.addWeight(weightInput.toDouble(), selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                            weightInput = ""
                        },
                        enabled = (weightInput.toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("Simpan penimbangan") }
                }
            }
        }
        item { ChartCard("Berat badan (kg)", weightPoints, AndroidColor.rgb(23, 107, 82), minimumY = null) }
        item { ChartCard("Kalori harian (kkal)", caloriePoints, AndroidColor.rgb(201, 111, 42), minimumY = 0f) }
        item { ChartCard("Kepatuhan puasa (%)", fastingPoints, AndroidColor.rgb(50, 117, 155), minimumY = 0f) }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } },
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun WeightTrendMessage(weights: List<WeightLogEntity>) {
    val lastThree = weights.takeLast(3)
    val tooFast = lastThree.size == 3 && lastThree.zipWithNext().all { (first, second) ->
        val days = Duration.ofMillis(second.measuredAtEpochMillis - first.measuredAtEpochMillis).toDays()
        val weeklyRate = if (days > 0) (first.weightKg - second.weightKg) * 7.0 / days else 0.0
        weeklyRate > 1.0
    }
    val stagnant = lastThree.size == 3 && run {
        val days = Duration.ofMillis(lastThree.last().measuredAtEpochMillis - lastThree.first().measuredAtEpochMillis).toDays()
        days in 14..28 && lastThree.first().weightKg - lastThree.last().weightKg < 0.3
    }
    when {
        tooFast -> AlertCard(
            "Penurunan cukup cepat",
            "Berat turun lebih dari 1 kg/minggu pada dua interval terakhir. Utamakan kesehatan dan konsultasikan dengan tenaga kesehatan.",
        )
        stagnant -> AlertCard(
            "Berat belum banyak berubah",
            "Jika pola ini berlangsung 2-3 minggu, pertimbangkan penyesuaian kecil 100-150 kkal bersama ahli gizi.",
        )
        else -> Text("Laju perubahan dipantau dari catatan mingguan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AlertCard(title: String, message: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ChartCard(title: String, points: List<ChartPoint>, lineColor: Int, minimumY: Float?) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (points.isEmpty()) {
                Text("Belum ada data untuk ditampilkan.", modifier = Modifier.padding(vertical = 24.dp))
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            axisRight.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.granularity = 1f
                            setTouchEnabled(true)
                            setPinchZoom(true)
                        }
                    },
                    update = { chart ->
                        val entries = points.mapIndexed { index, point -> Entry(index.toFloat(), point.value) }
                        val line = LineDataSet(entries, title).apply {
                            color = lineColor
                            setCircleColor(lineColor)
                            lineWidth = 2f
                            circleRadius = 3.5f
                            setDrawValues(false)
                            setDrawFilled(true)
                            fillColor = lineColor
                            fillAlpha = 28
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                        }
                        chart.data = LineData(line)
                        chart.xAxis.valueFormatter = object : ValueFormatter() {
                            override fun getAxisLabel(value: Float, axis: AxisBase?): String =
                                points.getOrNull(value.roundToInt())?.label.orEmpty()
                        }
                        minimumY?.let { chart.axisLeft.axisMinimum = it }
                        chart.invalidate()
                    },
                )
            }
        }
    }
}

private fun format(value: Double): String = String.format(Locale("id", "ID"), "%.1f", value)

private fun formatSigned(value: Double): String = String.format(Locale("id", "ID"), "%+.1f", value)