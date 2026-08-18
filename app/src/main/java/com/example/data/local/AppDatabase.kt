package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AssignmentDao
import com.example.data.local.dao.CourseDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.entity.Assignment
import com.example.data.local.entity.Course
import com.example.data.local.entity.ScheduleBlock
import com.example.data.local.entity.StudySession

@Database(
    entities = [
        Course::class,
        Assignment::class,
        ScheduleBlock::class,
        StudySession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
