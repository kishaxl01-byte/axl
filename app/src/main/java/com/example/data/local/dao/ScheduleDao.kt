package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ScheduleBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_blocks ORDER BY startHour ASC, startMinute ASC")
    fun getAllScheduleBlocks(): Flow<List<ScheduleBlock>>

    @Query("SELECT * FROM schedule_blocks WHERE courseId = :courseId")
    fun getScheduleForCourse(courseId: Long): Flow<List<ScheduleBlock>>

    @Query("SELECT * FROM schedule_blocks WHERE id = :id")
    suspend fun getScheduleBlockById(id: Long): ScheduleBlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleBlock(block: ScheduleBlock): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blocks: List<ScheduleBlock>): List<Long>

    @Update
    suspend fun updateScheduleBlock(block: ScheduleBlock)

    @Delete
    suspend fun deleteScheduleBlock(block: ScheduleBlock)

    @Query("DELETE FROM schedule_blocks WHERE id = :id")
    suspend fun deleteScheduleBlockById(id: Long)
}
