package id.dietku.app.feature.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.MealPlanRepository
import id.dietku.app.data.local.MealPlanEntity
import id.dietku.app.data.local.ShoppingItemEntity
import id.dietku.app.domain.MenuPreset
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MealPlannerViewModel @Inject constructor(
    private val repository: MealPlanRepository,
) : ViewModel() {
    private val today = LocalDate.now()
    val plans = repository.observeWeek(today).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val shoppingItems = repository.observeShopping(today).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    init {
        viewModelScope.launch { repository.ensureWeek(today) }
    }

    fun selectMenu(date: LocalDate, menu: MenuPreset) {
        viewModelScope.launch { repository.assign(date, menu) }
    }

    fun randomizeWeek() {
        viewModelScope.launch { repository.randomizeWeek(today) }
    }

    fun makeShoppingList() {
        viewModelScope.launch { repository.buildShoppingList(today) }
    }

    fun toggleShoppingItem(item: ShoppingItemEntity, checked: Boolean) {
        viewModelScope.launch { repository.updateShoppingItem(item.copy(isChecked = checked)) }
    }

    fun plansFor(date: LocalDate, allPlans: List<MealPlanEntity>) =
        allPlans.filter { it.planDateEpochDay == date.toEpochDay() }
}