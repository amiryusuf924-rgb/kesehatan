package id.dietku.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT COUNT(*) FROM food_item")
    suspend fun count(): Int

    @Query("SELECT * FROM food_item ORDER BY isFrequent DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_item WHERE isFrequent = 1 ORDER BY name COLLATE NOCASE")
    fun observeFrequent(): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Insert
    suspend fun insert(item: FoodItemEntity): Long

    @Query("UPDATE food_item SET isFrequent = 1 WHERE id = :id")
    suspend fun markFrequent(id: Long)
}