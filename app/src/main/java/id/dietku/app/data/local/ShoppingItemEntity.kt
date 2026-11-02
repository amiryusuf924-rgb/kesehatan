package id.dietku.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_item",
    indices = [Index(value = ["weekStartEpochDay", "name"], unique = true)],
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartEpochDay: Long,
    val name: String,
    val isChecked: Boolean = false,
)