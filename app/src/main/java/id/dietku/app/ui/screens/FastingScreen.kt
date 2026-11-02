package id.dietku.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.dietku.app.data.local.FastingLogEntity
import id.dietku.app.domain.FastingWindowCalculator
import id.dietku.app.feature.fasting.FastingViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

private val fastingModes = listOf(12, 14, 16, 18)

@Composable
fun FastingScreen(viewModel: FastingViewModel = hiltViewModel()) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    var hourInput by remember(preferences.eatingStartHour) {
        mutableStateOf(preferences.eatingStartHour.toString())
    }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = ZonedDateTime.now()
        }
    }

    val today = now.toLocalDate()
    val fastingHours = preferences.fastingHoursFor(today)
    val state = FastingWindowCalculator.calculate(
        now = now,
        eatingStart = LocalTime.of(preferences.eatingStartHour, 0),
        fastingHours = fastingHours,
    )
    val eatingHours = 24 - fastingHours
    val timeRemaining = Duration.between(now, state.nextChange).coerceAtLeast(Duration.ZERO)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Puasa intermiten", style = MaterialTheme.typography.headlineSmall)
                Text("Jadwal terukur, fleksibel saat hari sedang padat.")
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        if (state.isEatingWindow) "Jendela makan" else "Waktu berpuasa",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Box(
                        modifier = Modifier
                            .size(176.dp)
                            .semantics {
                                contentDescription = "Progress puasa ${(state.elapsedFraction * 100).toInt()} persen"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = { state.elapsedFraction },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatDuration(timeRemaining), style = MaterialTheme.typography.headlineSmall)
                            Text(
                                if (state.isEatingWindow) "menuju mulai puasa" else "menuju waktu makan",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Text("${fastingHours}:$eatingHours  •  makan ${formatHour(preferences.eatingStartHour)}-${formatHour((preferences.eatingStartHour + eatingHours) % 24)}")
                    Text("Batas puasa DietKu: maksimal 18 jam.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Atur jadwal", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        fastingModes.forEach { hours ->
                            FilterChip(
                                selected = preferences.fastingHours == hours,
                                onClick = { viewModel.setSchedule(hours, preferences.eatingStartHour) },
                                label = { Text("$hours:${24 - hours}") },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = hourInput,
                        onValueChange = { hourInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("Mulai makan, pukul (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(
                        onClick = { viewModel.setSchedule(preferences.fastingHours, hourInput.toInt()) },
                        enabled = (hourInput.toIntOrNull() ?: -1) in 0..23,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Simpan jam makan") }
                    OutlinedButton(
                        onClick = viewModel::relaxToday,
                        enabled = preferences.relaxedDate != today.toString(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (preferences.relaxedDate == today.toString()) "Jadwal hari ini 14:10" else "Longgarkan hari ini ke 14:10")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (activeSession == null) {
                    Button(
                        onClick = viewModel::startFast,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("Mulai puasa") }
                } else {
                    Button(
                        onClick = viewModel::finishFast,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("Akhiri puasa") }
                }
            }
        }
        item {
            Text("Konsistensi 7 hari", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0..6).map { today.minusDays((6 - it).toLong()) }.forEach { date ->
                    FastingDay(
                        date = date,
                        session = sessions.firstOrNull {
                            java.time.Instant.ofEpochMilli(it.startEpochMillis)
                                .atZone(now.zone).toLocalDate() == date
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FastingDay(date: LocalDate, session: FastingLogEntity?, modifier: Modifier = Modifier) {
    val elapsedHours = session?.let { log ->
        val end = log.endEpochMillis ?: System.currentTimeMillis()
        Duration.ofMillis((end - log.startEpochMillis).coerceAtLeast(0L)).toMinutes() / 60.0
    }
    val status = when {
        session == null || elapsedHours == null -> "Belum"
        elapsedHours >= session.plannedHours.toDouble() -> "Tercapai"
        elapsedHours >= session.plannedHours * 0.75 -> "Sebagian"
        else -> "Belum"
    }
    val color = when (status) {
        "Tercapai" -> MaterialTheme.colorScheme.primary
        "Sebagian" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Column(
        modifier = modifier.padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("id", "ID")))
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .size(28.dp)
                .semantics { contentDescription = "$status, ${date.dayOfMonth}" },
        ) {
            drawCircle(color = color)
        }
    }
}

private fun formatDuration(duration: Duration): String = String.format(
    Locale.ROOT,
    "%02d:%02d:%02d",
    duration.toHours(),
    duration.toMinutes() % 60,
    duration.seconds % 60,
)

private fun formatHour(hour: Int): String = "%02d.00".format(hour)