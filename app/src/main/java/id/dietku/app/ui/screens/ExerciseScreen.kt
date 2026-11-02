package id.dietku.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.dietku.app.feature.exercise.ExerciseViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay

private val weekDays = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

@Composable
fun ExerciseScreen(viewModel: ExerciseViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val logs by viewModel.weekLogs.collectAsStateWithLifecycle()
    val baseline by viewModel.stepBaseline.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val todayLog = logs.firstOrNull {
        Instant.ofEpochMilli(it.loggedAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == today && it.completed
    }
    val (workoutName, defaultDuration) = ExerciseViewModel.workoutFor(today.dayOfWeek)
    var durationInput by remember(today, defaultDuration) { mutableStateOf((todayLog?.durationMinutes ?: defaultDuration).toString()) }
    var timerRunning by remember { mutableStateOf(false) }
    var briskPhase by remember { mutableStateOf(true) }
    var secondsRemaining by remember { mutableIntStateOf(60) }
    var stepCount by remember { mutableIntStateOf(0) }
    var permissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
    }
    val sensorManager = remember(context) { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val stepSensor = remember(sensorManager) { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1_000)
            if (secondsRemaining <= 1) {
                briskPhase = !briskPhase
                secondsRemaining = if (briskPhase) 60 else 120
            } else {
                secondsRemaining -= 1
            }
        }
    }

    DisposableEffect(stepSensor, permissionGranted, baseline.date, baseline.count) {
        if (stepSensor != null && permissionGranted) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val rawSteps = event.values.firstOrNull() ?: return
                    val todayKey = today.toString()
                    if (baseline.date != todayKey || baseline.count == null || rawSteps < baseline.count) {
                        viewModel.saveStepBaseline(todayKey, rawSteps)
                        stepCount = 0
                    } else {
                        stepCount = (rawSteps - baseline.count).toInt().coerceAtLeast(0)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        } else {
            onDispose { }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Olahraga", style = MaterialTheme.typography.headlineSmall)
                Text("Rencana ringan yang menyesuaikan ritme mingguan.")
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Outlined.DirectionsWalk, contentDescription = null)
                        Text("  Langkah hari ini", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("$stepCount langkah  •  target 6.000-8.000")
                    LinearProgressIndicator(
                        progress = { (stepCount / 6000f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Progress $stepCount dari 6000 langkah" },
                    )
                    when {
                        stepSensor == null -> Text("Sensor langkah tidak tersedia di perangkat ini.")
                        !permissionGranted -> OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                            },
                        ) { Text("Izinkan akses aktivitas fisik") }
                        else -> Text("Langkah dihitung dari sensor bawaan ponsel.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Jadwal minggu ini", style = MaterialTheme.typography.titleMedium)
                    weekDays.forEach { day ->
                        val (name, minutes) = ExerciseViewModel.workoutFor(day)
                        val date = today.with(DayOfWeek.MONDAY).plusDays((day.value - 1).toLong())
                        val completed = logs.any { log ->
                            log.completed && Instant.ofEpochMilli(log.loggedAtEpochMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate() == date
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = completed,
                                onCheckedChange = { checked ->
                                    viewModel.setWorkoutCompleted(
                                        date,
                                        checked,
                                        if (date == today) durationInput.toIntOrNull() ?: minutes else minutes,
                                    )
                                },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(day.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID")))
                                Text("$name  •  $minutes menit", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (todayLog != null) Text("Latihan hari ini selesai, ${todayLog.durationMinutes} menit.")
                    else {
                        OutlinedTextField(
                            value = durationInput,
                            onValueChange = { durationInput = it.filter(Char::isDigit).take(3) },
                            label = { Text("Durasi hari ini (menit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { viewModel.completeWorkout(durationInput.toInt()) },
                            enabled = durationInput.toIntOrNull() in 1..240,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Tandai selesai") }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Timer jalan interval", style = MaterialTheme.typography.titleMedium)
                    Text("${if (briskPhase) "Jalan cepat" else "Jalan santai"}  •  ${secondsRemaining / 60}:${(secondsRemaining % 60).toString().padStart(2, '0')}")
                    Text("Pola 1 menit cepat, 2 menit santai.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { timerRunning = !timerRunning }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (timerRunning) "Jeda timer" else "Mulai timer")
                    }
                }
            }
        }
    }
}