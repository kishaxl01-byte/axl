package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // e.g. "CS 101" or "AP BIO"
    val name: String, // e.g. "Intro to Computer Science"
    val instructor: String = "",
    val colorHex: String = "#4F46E5",
    val roomLocation: String = "",
    val credits: Float = 3.0f,
    val targetGrade: String = "A",
    val term: String = "Fall 2026",
    val notes: String = ""
)
