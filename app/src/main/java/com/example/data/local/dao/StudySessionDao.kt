package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestampMillis DESC")
    fun getAllStudySessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE courseId = :courseId ORDER BY timestampMillis DESC")
    fun getStudySessionsForCourse(courseId: Long): Flow<List<StudySession>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE timestampMillis >= :sinceMillis")
    fun getTotalFocusMinutesSince(sinceMillis: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}
