package id.dietku.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EducationScreen() {
    val recommended = listOf(
        "Nasi dengan porsi terukur, nasi merah, jagung, ubi, atau singkong rebus",
        "Tempe, tahu, telur, ikan, dada ayam, dan kacang hijau",
        "Sayur beragam dan buah utuh",
        "Air putih, kopi atau teh tanpa gula",
    )
    val limit = listOf(
        "Minuman manis dan kopi sachet",
        "Gorengan, santan pekat, kue, dan roti manis",
        "Mi instan, sosis, nugget, dan saus berlebih",
        "Makan larut malam",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Panduan", style = MaterialTheme.typography.headlineSmall)
        }
        item { ListCard("Utamakan", recommended) }
        item { ListCard("Kurangi", limit) }
        item {
            ListCard(
                "Jaga keamanan",
                listOf(
                    "Hentikan puasa jika pusing berat, gemetar, atau hampir pingsan.",
                    "Usahakan tidur 6,5-7 jam.",
                    "Batasi kopi maksimal 2 cangkir dan hindari setelah pukul 15.00.",
                    "Pertimbangkan cek gula darah, tekanan darah, dan kolesterol.",
                ),
            )
        }
        item {
            Text(
                "DietKu adalah panduan umum, bukan pengganti dokter atau ahli gizi. " +
                    "Diskusikan kebutuhan khusus dan target kalori dengan tenaga kesehatan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ListCard(title: String, entries: List<String>) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            entries.forEach { entry ->
                Text("•  $entry", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}