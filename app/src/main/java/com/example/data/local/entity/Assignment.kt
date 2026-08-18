package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["courseId"]), Index(value = ["dueDateMillis"])]
)
data class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseId: Long? = null,
    val type: AssignmentType = AssignmentType.HOMEWORK,
    val priority: Priority = Priority.MEDIUM,
    val status: AssignmentStatus = AssignmentStatus.TODO,
    val dueDateMillis: Long, // timestamp in millis
    val estimatedMinutes: Int = 60,
    val subtasksJson: String = "[]", // JSON serialized List<Subtask>
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val reminderAdvanceMinutes: Int = 1440, // default 24h (1440 min)
    val isExportedToCalendar: Boolean = false,
    val completionDateMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
