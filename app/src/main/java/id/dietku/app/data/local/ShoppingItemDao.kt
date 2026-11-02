package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_item WHERE weekStartEpochDay = :weekStart ORDER BY name COLLATE NOCASE")
    fun observeWeek(weekStart: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_item ORDER BY weekStartEpochDay, name COLLATE NOCASE")
    suspend fun getAll(): List<ShoppingItemEntity>

    @Query("DELETE FROM shopping_item WHERE weekStartEpochDay = :weekStart")
    suspend fun clearWeek(weekStart: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Update
    suspend fun update(item: ShoppingItemEntity)
}