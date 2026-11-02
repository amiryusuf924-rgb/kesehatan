package id.dietku.app.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import id.dietku.app.data.local.FoodItemEntity
import id.dietku.app.feature.food.FoodLoggingViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

private val mealTypes = listOf("Makan 1", "Makan 2", "Selingan")

@Composable
fun FoodLoggingScreen(viewModel: FoodLoggingViewModel = hiltViewModel()) {
    val foods by viewModel.foods.collectAsStateWithLifecycle()
    val logs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val fastingPreferences by viewModel.fastingPreferences.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(mealTypes.first()) }
    var quantity by remember { mutableStateOf("1") }
    var showCustomFood by remember { mutableStateOf(false) }
    var showOutsideWindowNote by remember { mutableStateOf(false) }

    val filteredFoods = foods.filter { it.name.contains(search.trim(), ignoreCase = true) }
    val totalCalories = logs.sumOf { it.caloriesKcal }
    val totalProtein = logs.sumOf { it.proteinGrams }
    val today = LocalDate.now()
    val fastingHours = fastingPreferences.fastingHoursFor(today)
    val eatingStart = LocalTime.of(fastingPreferences.eatingStartHour, 0)
    val eatingEnd = eatingStart.plusHours((24 - fastingHours).toLong())
    val now = LocalTime.now()
    val isInsideEatingWindow = if (eatingEnd.isAfter(eatingStart)) {
        !now.isBefore(eatingStart) && now.isBefore(eatingEnd)
    } else {
        !now.isBefore(eatingStart) || now.isBefore(eatingEnd)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Catat makan", style = MaterialTheme.typography.headlineSmall)
                Text("${totalCalories.toInt()} kkal  •  ${format(totalProtein)} g protein hari ini")
            }
        }
        if (showOutsideWindowNote) {
            item {
                Text(
                    "Catatan tetap tersimpan. Waktu makan ini berada di luar jendela yang kamu atur.",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            OutlinedButton(
                onClick = viewModel::copyYesterday,
                enabled = logs.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salin makanan dari kemarin") }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Waktu makan", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    mealTypes.forEach { type ->
                        FilterChip(
                            selected = mealType == type,
                            onClick = { mealType = type },
                            label = { Text(type) },
                        )
                    }
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Jumlah porsi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Cari makanan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = { showCustomFood = true }) {
                    Text("Tambah makanan custom")
                }
            }
        }
        if (logs.isNotEmpty()) {
            item {
                Text("Tercatat hari ini", style = MaterialTheme.typography.titleMedium)
            }
            items(logs, key = { "log-${it.id}" }) { log ->
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.foodName, style = MaterialTheme.typography.titleSmall)
                            Text("${log.mealType}  •  ${format(log.quantity)} porsi")
                            Text("${log.caloriesKcal.toInt()} kkal  •  ${format(log.proteinGrams)} g protein")
                        }
                        TextButton(onClick = { viewModel.deleteLog(log.id) }) {
                            Text("Hapus")
                        }
                    }
                }
            }
        }
        item {
            Text(
                if (search.isBlank()) "Sering dimakan & katalog" else "Hasil pencarian",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(filteredFoods, key = { "food-${it.id}" }) { food ->
            FoodItemCard(
                food = food,
                onAdd = {
                    quantity.toDoubleOrNull()?.let {
                        viewModel.logFood(food, it, mealType)
                        if (!isInsideEatingWindow) showOutsideWindowNote = true
                    }
                },
                canAdd = (quantity.toDoubleOrNull() ?: 0.0) > 0.0,
            )
        }
        if (filteredFoods.isEmpty()) {
            item { Text("Makanan tidak ditemukan. Kamu bisa menambahkannya sebagai makanan custom.") }
        }
    }

    if (showCustomFood) {
        CustomFoodDialog(
            onDismiss = { showCustomFood = false },
            onSave = { food ->
                viewModel.addCustomFood(food)
                showCustomFood = false
            },
        )
    }
}

@Composable
private fun FoodItemCard(food: FoodItemEntity, onAdd: () -> Unit, canAdd: Boolean) {
    val categoryColor = when (food.category) {
        "Dianjurkan" -> MaterialTheme.colorScheme.primary
        "Batasi" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(food.name, style = MaterialTheme.typography.titleSmall)
                Text(food.servingDescription, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${food.caloriesKcal.toInt()} kkal  •  P ${format(food.proteinGrams)} g  •  " +
                        "K ${format(food.carbohydrateGrams)} g  •  L ${format(food.fatGrams)} g",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    if (food.isFrequent) "Sering dimakan  •  ${food.category}" else food.category,
                    color = categoryColor,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            TextButton(onClick = onAdd, enabled = canAdd) { Text("Tambah") }
        }
    }
}

@Composable
private fun CustomFoodDialog(
    onDismiss: () -> Unit,
    onSave: (FoodItemEntity) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var serving by remember { mutableStateOf("1 porsi") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("0") }
    var carbohydrates by remember { mutableStateOf("0") }
    var fat by remember { mutableStateOf("0") }
    val calorieValue = calories.toDoubleOrNull()
    val proteinValue = protein.toDoubleOrNull()
    val carbohydrateValue = carbohydrates.toDoubleOrNull()
    val fatValue = fat.toDoubleOrNull()
    val valid = name.isNotBlank() && serving.isNotBlank() &&
        calorieValue != null && calorieValue >= 0.0 &&
        proteinValue != null && proteinValue >= 0.0 &&
        carbohydrateValue != null && carbohydrateValue >= 0.0 &&
        fatValue != null && fatValue >= 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Makanan custom") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CustomField("Nama", name) { name = it }
                CustomField("Takaran", serving) { serving = it }
                CustomField("Kalori (kkal)", calories, KeyboardType.Decimal) { calories = it }
                CustomField("Protein (g)", protein, KeyboardType.Decimal) { protein = it }
                CustomField("Karbohidrat (g)", carbohydrates, KeyboardType.Decimal) { carbohydrates = it }
                CustomField("Lemak (g)", fat, KeyboardType.Decimal) { fat = it }
                Text("Makanan custom diberi label Secukupnya.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        FoodItemEntity(
                            name = name.trim(),
                            servingDescription = serving.trim(),
                            caloriesKcal = calorieValue!!,
                            proteinGrams = proteinValue!!,
                            carbohydrateGrams = carbohydrateValue!!,
                            fatGrams = fatValue!!,
                            category = "Secukupnya",
                            isCustom = true,
                        ),
                    )
                },
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun CustomField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun format(value: Double): String = String.format(Locale("id", "ID"), "%.1f", value)