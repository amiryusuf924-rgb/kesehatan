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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.dietku.app.data.local.MealPlanEntity
import id.dietku.app.domain.MenuCatalog
import id.dietku.app.domain.MenuPreset
import id.dietku.app.feature.mealplan.MealPlannerViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val plannerSlots = listOf("Makan 1", "Makan 2", "Bekal", "Darurat", "Selingan")

@Composable
fun MealPlannerScreen(viewModel: MealPlannerViewModel = hiltViewModel()) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val weekStart = LocalDate.now().with(DayOfWeek.MONDAY)
    var pendingSelection by remember { mutableStateOf<Pair<LocalDate, String>?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Rencana menu", style = MaterialTheme.typography.headlineSmall)
                Text("Minggu ${weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale("id", "ID")))}")
            }
        }
        item {
            OutlinedButton(onClick = viewModel::randomizeWeek, modifier = Modifier.fillMaxWidth()) {
                Text("Acak menu dengan variasi protein")
            }
        }
        items((0..6).map { weekStart.plusDays(it.toLong()) }, key = { it.toEpochDay() }) { date ->
            val dayPlans = plans.filter { it.planDateEpochDay == date.toEpochDay() }
            DayMenuCard(
                date = date,
                plans = dayPlans,
                onSelectSlot = { slot -> pendingSelection = date to slot },
            )
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daftar belanja", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = viewModel::makeShoppingList, modifier = Modifier.fillMaxWidth()) {
                        Text("Buat daftar belanja otomatis")
                    }
                    if (shoppingItems.isEmpty()) {
                        Text("Daftar dibuat dari bahan menu yang sudah direncanakan.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        shoppingItems.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { viewModel.toggleShoppingItem(item, it) },
                                )
                                Text(
                                    item.name,
                                    style = if (item.isChecked) MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ) else MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingSelection?.let { (date, slot) ->
        val options = MenuCatalog.forSlot(slot)
        AlertDialog(
            onDismissRequest = { pendingSelection = null },
            title = { Text("Pilih $slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    options.forEach { option ->
                        TextButton(
                            onClick = {
                                viewModel.selectMenu(date, option)
                                pendingSelection = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(option.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${option.caloriesKcal} kkal  •  ${option.proteinGrams.toInt()} g protein",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pendingSelection = null }) { Text("Tutup") } },
        )
    }
}

@Composable
private fun DayMenuCard(
    date: LocalDate,
    plans: List<MealPlanEntity>,
    onSelectSlot: (String) -> Unit,
) {
    val calories = plans.sumOf { it.caloriesKcal }
    val protein = plans.sumOf { it.proteinGrams }
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("id", "ID"))),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text("$calories kkal  •  ${protein.toInt()} g P", style = MaterialTheme.typography.labelMedium)
            }
            plannerSlots.forEach { slot ->
                val plan = plans.firstOrNull { it.mealSlot == slot }
                TextButton(onClick = { onSelectSlot(slot) }, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(if (plan == null) "+ $slot" else "$slot  •  ${plan.menuName}")
                        if (plan != null) {
                            Text("${plan.caloriesKcal} kkal  •  ${plan.proteinGrams.toInt()} g protein", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}