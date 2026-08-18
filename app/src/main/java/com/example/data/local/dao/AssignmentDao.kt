package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.AssignmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY dueDateMillis ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE status != :status ORDER BY dueDateMillis ASC")
    fun getPendingAssignments(status: AssignmentStatus = AssignmentStatus.COMPLETED): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDateMillis ASC")
    fun getAssignmentsForCourse(courseId: Long): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Long): Assignment?

    @Query("SELECT * FROM assignments WHERE id = :id")
    fun getAssignmentByIdFlow(id: Long): Flow<Assignment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assignments: List<Assignment>): List<Long>

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)

    @Query("UPDATE assignments SET status = :status, completionDateMillis = :completionTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: AssignmentStatus, completionTime: Long?)

    @Query("UPDATE assignments SET isExportedToCalendar = :isExported WHERE id = :id")
    suspend fun updateCalendarExportStatus(id: Long, isExported: Boolean)
}
