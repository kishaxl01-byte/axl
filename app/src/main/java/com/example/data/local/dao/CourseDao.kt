package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Course
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseByIdFlow(id: Long): Flow<Course?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>): List<Long>

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Long)
}
