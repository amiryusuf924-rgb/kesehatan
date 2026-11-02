package id.dietku.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.dietku.app.feature.settings.CsvExportViewModel

@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    exportViewModel: CsvExportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    LaunchedEffect(exportViewModel) {
        exportViewModel.exportedFiles.collect { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Ekspor data DietKu"))
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Lainnya", style = MaterialTheme.typography.headlineSmall) }
        item {
            MoreOption(
                title = "Profil dan target",
                description = "Ubah data diri, target kalori, dan tingkat aktivitas.",
                onClick = { onNavigate("profile") },
            )
        }
        item {
            MoreOption(
                title = "Rencana menu dan belanja",
                description = "Atur menu mingguan dan susun daftar belanja.",
                onClick = { onNavigate("meal-plan") },
            )
        }
        item {
            MoreOption(
                title = "Olahraga dan langkah",
                description = "Checklist mingguan, timer jalan, dan sensor langkah.",
                onClick = { onNavigate("exercise") },
            )
        }
        item {
            MoreOption(
                title = "Pengingat",
                description = "Atur notifikasi, interval minum, dan jam mengajar.",
                onClick = { onNavigate("reminders") },
            )
        }
        item {
            MoreOption(
                title = "Panduan dan keamanan",
                description = "Pilihan makanan, tips, dan informasi kesehatan.",
                onClick = { onNavigate("education") },
            )
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ekspor data", style = MaterialTheme.typography.titleMedium)
                    Text("Buat file CSV dari catatan makan, puasa, air, olahraga, dan berat.")
                    Button(onClick = exportViewModel::export, modifier = Modifier.fillMaxWidth()) {
                        Text("Bagikan CSV")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("DietKu", style = MaterialTheme.typography.titleMedium)
                    Text("Data tersimpan di perangkat ini dan tidak memerlukan akun.")
                    Text("Versi 0.1.0  •  Tahap pengembangan awal", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MoreOption(title: String, description: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}