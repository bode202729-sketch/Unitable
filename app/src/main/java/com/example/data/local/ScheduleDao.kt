package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_items ORDER BY startTime ASC")
    fun getAllItems(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items ORDER BY startTime ASC")
    fun getAllItemsSync(): List<ScheduleItem>

    @Query("SELECT * FROM schedule_items WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getItemsForDay(day: String): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getItemsForDaySync(day: String): List<ScheduleItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScheduleItem>)

    @Update
    suspend fun updateItem(item: ScheduleItem)

    @Delete
    suspend fun deleteItem(item: ScheduleItem)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM schedule_items WHERE subject IN (:subjects)")
    suspend fun deleteBySubjects(subjects: List<String>)

    @Query("DELETE FROM schedule_items")
    suspend fun clearAll()
}
