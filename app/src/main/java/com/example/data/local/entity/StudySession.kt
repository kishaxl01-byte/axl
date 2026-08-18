package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Assignment::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["courseId"]), Index(value = ["assignmentId"]), Index(value = ["timestampMillis"])]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long? = null,
    val assignmentId: Long? = null,
    val durationMinutes: Int = 25,
    val timestampMillis: Long = System.currentTimeMillis(),
    val focusScore: Int = 5, // 1 to 5 stars
    val notes: String = ""
)
