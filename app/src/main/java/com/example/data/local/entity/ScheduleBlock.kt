package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_blocks",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class ScheduleBlock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseId: Long? = null,
    val activityType: ActivityType = ActivityType.LECTURE,
    val daysOfWeek: String = "1,3,5", // Comma-separated Java Calendar days: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
    val startHour: Int = 10, // 0-23
    val startMinute: Int = 0, // 0-59
    val endHour: Int = 11,
    val endMinute: Int = 15,
    val location: String = "Room 204",
    val instructor: String = "",
    val isRecurring: Boolean = true,
    val specificDateMillis: Long? = null, // for one-off sessions
    val reminderMinutesBefore: Int = 15,
    val colorHex: String = "#4F46E5",
    val notes: String = ""
)
