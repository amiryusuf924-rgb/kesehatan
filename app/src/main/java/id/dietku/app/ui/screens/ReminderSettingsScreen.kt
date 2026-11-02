package id.dietku.app.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.dietku.app.data.local.ReminderSettingEntity
import id.dietku.app.feature.settings.ReminderSettingsViewModel

@Composable
fun ReminderSettingsScreen(viewModel: ReminderSettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val quietHours by viewModel.quietHours.collectAsStateWithLifecycle()
    var quietStart by remember(quietHours) { mutableStateOf(quietHours.first.toString()) }
    var quietEnd by remember(quietHours) { mutableStateOf(quietHours.second.toString()) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Pengingat", style = MaterialTheme.typography.headlineSmall)
                Text("Diatur lokal dan tetap aktif setelah perangkat dinyalakan kembali.")
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Izin notifikasi", style = MaterialTheme.typography.titleMedium)
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        OutlinedButton(
                            onClick = { notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Izinkan notifikasi") }
                    } else {
                        Text("Izin notifikasi tersedia.")
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.parse("package:${context.packageName}"),
                                        ),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Izinkan alarm tepat waktu") }
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Jangan ganggu saat mengajar", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quietStart,
                            onValueChange = { quietStart = it.filter(Char::isDigit).take(2) },
                            label = { Text("Mulai jam") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = quietEnd,
                            onValueChange = { quietEnd = it.filter(Char::isDigit).take(2) },
                            label = { Text("Sampai jam") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text("Jam lokal 0-23. Pengingat akan ditunda atau dilewati pada rentang ini.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { viewModel.setQuietHours(quietStart.toInt(), quietEnd.toInt()) },
                        enabled = (quietStart.toIntOrNull() ?: -1) in 0..23 && (quietEnd.toIntOrNull() ?: -1) in 0..23,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Simpan jam jangan ganggu") }
                }
            }
        }
        items(reminders, key = { it.reminderType }) { setting ->
            ReminderSettingCard(setting = setting, onSave = viewModel::save)
        }
    }
}

@Composable
private fun ReminderSettingCard(setting: ReminderSettingEntity, onSave: (ReminderSettingEntity) -> Unit) {
    var timeInput by remember(setting) {
        mutableStateOf("%02d:%02d".format(setting.hour, setting.minute))
    }
    var intervalInput by remember(setting) {
        mutableStateOf(setting.intervalMinutes.coerceAtLeast(60).toString())
    }
    val timeParts = timeInput.split(":")
    val hour = timeParts.getOrNull(0)?.toIntOrNull()
    val minute = timeParts.getOrNull(1)?.toIntOrNull()
    val timeValid = hour != null && hour in 0..23 && minute != null && minute in 0..59
    val interval = intervalInput.toIntOrNull()

    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(labelFor(setting.reminderType), style = MaterialTheme.typography.titleSmall)
                    Text(descriptionFor(setting.reminderType), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = setting.enabled,
                    onCheckedChange = { onSave(setting.copy(enabled = it)) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it.filter { char -> char.isDigit() || char == ':' }.take(5) },
                    label = { Text(if (setting.reminderType == "water") "Mulai aktif" else "Waktu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                if (setting.reminderType == "water") {
                    OutlinedTextField(
                        value = intervalInput,
                        onValueChange = { intervalInput = it.filter(Char::isDigit).take(3) },
                        label = { Text("Interval menit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    onSave(
                        setting.copy(
                            hour = hour!!,
                            minute = minute!!,
                            intervalMinutes = if (setting.reminderType == "water") interval!! else setting.intervalMinutes,
                        ),
                    )
                },
                enabled = timeValid && (setting.reminderType != "water" || interval?.let { it in 15..240 } == true),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Simpan jadwal") }
        }
    }
}

private fun labelFor(type: String): String = when (type) {
    "meal_start" -> "Mulai jendela makan"
    "window_close" -> "Jendela makan segera ditutup"
    "fast_start" -> "Mulai puasa"
    "water" -> "Minum air"
    "exercise" -> "Olahraga"
    "weight" -> "Timbang berat hari Minggu"
    "meal_plan" -> "Rencana menu dan belanja"
    else -> "Bersiap tidur"
}

private fun descriptionFor(type: String): String = when (type) {
    "window_close" -> "Disarankan 30 menit sebelum waktu makan berakhir."
    "water" -> "Aktif mulai jam pilihan sampai pukul 21.00."
    "weight", "meal_plan" -> "Berulang setiap hari Minggu."
    "sleep" -> "Pengingat waktu istirahat malam."
    else -> "Pengingat harian sesuai jam pilihan."
}